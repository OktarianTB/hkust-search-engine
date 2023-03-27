## COMP4321 Search Engine

### How to run the crawler & indexer

The crawling and indexing is found in the `Crawler.java` file. The test program is found in the `App.java` file.

The easiest way to run the programs is to load the project in VS Code (with Java extension pack installed), right click on the file and click 'Run Java'. Alternatively, the following commands can be used from any terminal:

To compile all files:
```
javac -d outputs -cp lib/jdbm-1.0.jar;lib/htmlparser.jar src/utilities/*.java src/storage/*.java src/crawler/*.java src/App.java
```

To execute the Crawler:
```
java -cp lib/jdbm-1.0.jar;lib/htmlparser.jar;outputs;. crawler.Crawler
```

To execute the test program:
```
java -cp lib/jdbm-1.0.jar;lib/htmlparser.jar;outputs;. App
```

