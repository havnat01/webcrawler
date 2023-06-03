package com.udacity.webcrawler;

import java.util.*;

import static java.lang.Math.min;
import static java.util.stream.Collectors.toMap;

/**
 * Utility class that sorts the map of word counts.
 */
final class WordCounts {

  /**
   * Given an unsorted map of word counts, returns a new map whose word counts are sorted according
   * to the provided {@link WordCountComparator}, and includes only the top
   * {@param popularWordCount} words and counts.
   * @param wordCounts       the unsorted map of word counts.
   * @param popularWordCount the number of popular words to include in the result map.
   * @return a map containing the top {@param popularWordCount} words and counts in the right order.
   */
  static Map<String, Integer> sort(Map<String, Integer> wordCounts, int popularWordCount) {
    List<Map.Entry<String, Integer>> entries = new ArrayList<>(wordCounts.entrySet());
    entries.sort(new WordCountComparator());
    int limit = Math.min(popularWordCount, entries.size());
    Map<String, Integer> sortedWordCounts = new LinkedHashMap<>();
    for (int i = 0; i < limit; i++) {
      Map.Entry<String, Integer> entry = entries.get(i);
      sortedWordCounts.put(entry.getKey(), entry.getValue());
    }
    return sortedWordCounts;
  }

  /**
   * A {@link Comparator} that sorts word count pairs correctly:
   *
   * <p>
   * <ol>
   *   <li>First sorting by word count, ranking more frequent words higher.</li>
   *   <li>Then sorting by word length, ranking longer words higher.</li>
   *   <li>Finally, breaking ties using alphabetical order.</li>
   * </ol>
   */
  private static final class WordCountComparator implements Comparator<Map.Entry<String, Integer>> {
    @Override
    public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
      int valueCompare = Integer.compare(b.getValue(), a.getValue());
      if (valueCompare != 0) {
        return valueCompare;
      }

      int lengthCompare = Integer.compare(b.getKey().length(), a.getKey().length());
      if (lengthCompare != 0) {
        return lengthCompare;
      }

      return a.getKey().compareTo(b.getKey());
    }
  }

  private WordCounts() {
    // This class cannot be instantiated
  }
}