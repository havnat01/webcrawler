package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

public final class CrawlerRecursiveService extends RecursiveTask<Boolean> {

    private final String url;
    private final Instant deadline;
    private final int maxDepth;
    private final ConcurrentMap<String, Integer> counts;
    private final ConcurrentSkipListSet<String> visitedUrls;
    private final Clock clock;
    private final PageParserFactory pageParserFactory;
    private final List<Pattern> ignoredUrls;

    public CrawlerRecursiveService(
            String url,
            Instant deadline,
            int maxDepth,
            ConcurrentMap<String, Integer> counts,
            ConcurrentSkipListSet<String> visitedUrls,
            Clock clock,
            PageParserFactory parserFactory,
            List<Pattern> ignoredUrls) {

        this.url = url;
        this.deadline = deadline;
        this.maxDepth = maxDepth;
        this.counts = counts;
        this.visitedUrls = visitedUrls;
        this.clock = clock;
        this.pageParserFactory = parserFactory;
        this.ignoredUrls = ignoredUrls;
    }

    private Optional<PageParser.Result> parseUrl(String url) {
        try {
            return Optional.of(pageParserFactory.get(url).parse());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    protected Boolean compute() {
        if (maxDepth == 0 || clock.instant().isAfter(deadline)) {
            return false;
        }
        for (Pattern pattern : ignoredUrls) {
            if (pattern.matcher(url).matches()) {
                return false;
            }
        }

        synchronized (visitedUrls) {
            if (visitedUrls.contains(url)) {
                return false;
            }
            visitedUrls.add(url);
        }

        Optional<PageParser.Result> resultOpt = parseUrl(url);
        if (resultOpt.isPresent()) {
            PageParser.Result result = resultOpt.get();
            for (ConcurrentMap.Entry<String, Integer> e : result.getWordCounts().entrySet()) {
                counts.compute(e.getKey(), (k, v) -> (v == null) ? e.getValue() : e.getValue() + v);
            }

            List<CrawlerRecursiveService> subtasks = new ArrayList<>();
            for (String link : result.getLinks()) {
                subtasks.add(new CrawlerRecursiveService(link, deadline, maxDepth - 1, counts,
                        visitedUrls, clock, pageParserFactory, ignoredUrls));
            }
            invokeAll(subtasks);
            return true;
        } else {
            return false;
        }
    }
}
