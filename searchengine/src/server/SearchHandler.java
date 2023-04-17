package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import engine.Engine;
import utilities.Result;

class SearchHandler implements HttpHandler {
    Gson gson;

    SearchHandler() throws IOException {
        gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Engine engine = new Engine();

        String requestMethod = exchange.getRequestMethod();

        if (requestMethod.equalsIgnoreCase("POST")) {
            // get the request body as a JsonObject
            InputStream requestBody = exchange.getRequestBody();
            JsonObject jsonObject = gson.fromJson(new InputStreamReader(requestBody), JsonObject.class);

            // get the "query" property from the JsonObject
            JsonElement queryElement = jsonObject.get("query");
            if (queryElement == null) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }
            String query = queryElement.getAsString();

            // search the engine
            long startTime = System.currentTimeMillis();
            List<Result> results = engine.search(query);
            long endTime = System.currentTimeMillis();

            // convert the results to a list of JsonObjects
            List<JsonObject> resultsJson = results.stream().map(Result::toJson)
                    .collect(java.util.stream.Collectors.toList());

            // create json response object
            JsonObject responseBodyJson = new JsonObject();
            responseBodyJson.add("results", gson.toJsonTree(resultsJson));
            responseBodyJson.addProperty("time", Math.round(endTime - startTime));

            // set the response headers
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "http://localhost:3000");
            headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);

            // send the response body as a JsonObject
            OutputStream responseBody = exchange.getResponseBody();
            OutputStreamWriter writer = new OutputStreamWriter(responseBody);
            gson.toJson(responseBodyJson, writer);
            writer.flush();
            writer.close();
        } else if (requestMethod.equalsIgnoreCase("OPTIONS")) {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "http://localhost:3000");
            headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");

            exchange.sendResponseHeaders(200, -1);
            return;
        } else {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Access-Control-Allow-Origin", "http://localhost:3000");
            headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type");

            exchange.sendResponseHeaders(405, -1);
        }

        engine.commitAndClose();
    }

}
