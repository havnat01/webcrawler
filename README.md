# Parallel Web Crawler

## Usage    
### Cloning

```
git clone https://github.com/xichen-de/ParallelWebCrawler.git
```

### Build and test

Use Maven to compile and run the unit tests:

```
mvn compile
mvn test
```

### Build JAR package

```
mvn package
```

### Run on the real website

```
mvn package
java -cp target/udacity-webcrawler-1.0.jar com.udacity.webcrawler.main.WebCrawlerMain src/main/config/example_config.json
```
