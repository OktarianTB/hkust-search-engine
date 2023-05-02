## COMP4321 Search Engine

There are multiple parts to the search engine:
- Backend (in /searchengine):
    - Crawling & Indexing
    - Search Engine (retrieval, ranking, etc)
    - Search Engine Server
- Frontend (in /frontend):
    - Search Engine Web Interface

### How to run the crawler & indexer (in /searchengine)

The main crawling and indexing file is the `crawler/Crawler.java` file.

The easiest way to run the programs is to load the project in VS Code (with Java extension pack installed), right click on the file and click 'Run Java'. Alternatively, the following commands can be used in Windows from any terminal:

To compile all Java files:
```
javac -d outputs -cp lib/jdbm-1.0.jar;lib/htmlparser.jar;lib/junit-4.13.2.jar;lib/mockito-core-3.12.4.jar;lib/gson-2.10.1.jar src/utilities/*.java src/storage/*.java src/crawler/*.java
```

To execute the Crawler:
```
java -cp lib/jdbm-1.0.jar;lib/htmlparser.jar;lib/junit-4.13.2.jar;lib/mockito-core-3.12.4.jar;lib/gson-2.10.1.jar;outputs;. crawler.Crawler
```
*Note: crawling 300 documents takes around ~10 seconds on my PC*

#### Important files

- `engine/Crawler.java`: responsible for crawling pages and indexing them
- `engine/PageParser.java`: responsible for crawling URLs and parsing pages
- `engine/Indexer.java`: responsible for managing storage, updating maps, and outputting results

`storage/Map.java` is the core generic base class which wraps around the HTree from the JDBM library. All other map classes are derived from this base class and specify the key and value types.

### How to run the search engine backend (in /searchengine)

The main search engine file is the `engine/Engine.java` file.

The easiest way to run the programs is to load the project in VS Code (with Java extension pack installed), right click on the file and click 'Run Java'. Alternatively, the following commands can be used in Windows from any terminal:

To compile all Java files:
```
javac -d outputs -cp lib/jdbm-1.0.jar;lib/htmlparser.jar;lib/junit-4.13.2.jar;lib/mockito-core-3.12.4.jar;lib/gson-2.10.1.jar src/utilities/*.java src/storage/*.java src/engine/*.java src/server/*.java src/App.java
```

To execute the search engine command-line version (only supports query search, not similar documents search):
```
java -cp lib/jdbm-1.0.jar;lib/htmlparser.jar;lib/junit-4.13.2.jar;lib/mockito-core-3.12.4.jar;lib/gson-2.10.1.jar;outputs;. engine.Engine
```

To execute the search engine server:
```
java -cp lib/jdbm-1.0.jar;lib/htmlparser.jar;lib/junit-4.13.2.jar;lib/mockito-core-3.12.4.jar;lib/gson-2.10.1.jar;outputs;. App
```

#### Important files
- `engine/Engine.java`: responsible for the core search logic
- `engine/Retriever.java`: responsible for interfacing with the storage layer
- `engine/TfIdf.java`: responsible for TfIdf calculations
- `engine/CosineSimilarity.java`: responsible for calculating the similarity between query and document vectors
- `server/*.java`: search engine API server

### How to run the search engine web interface (in /frontend)

The web interface is a dynamic React application. The server must be running first.

Assuming you have NPM installed, first install the NPM packages for the project by running the following commands in the `frontend` directory:

```
npm install
```

Then to run the project, use:

```
npm start
```

After initialization, a page should open in your browser. The frontend requires the backend server to be running to make API requests.
