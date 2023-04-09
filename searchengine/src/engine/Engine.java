package engine;

import java.util.ArrayList;
import java.util.List;

import utilities.Result;

class Engine {

    public Engine() {

    }

    public List<Result> search(String query) {
        return new ArrayList<Result>();
    }

    public static void main(String[] args) throws Exception {
        Engine searchEngine = new Engine();
        searchEngine.search("test");
    }
}
