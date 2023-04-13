package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class SearchHandler implements HttpHandler {
    private Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
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

            // process the query
            String response = "You searched for: " + query;

            // create json response object
            JsonObject responseBodyJson = new JsonObject();
            responseBodyJson.addProperty("message", response);
            
            // set the response headers
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);

            // send the response body as a JsonObject
            OutputStream responseBody = exchange.getResponseBody();
            OutputStreamWriter writer = new OutputStreamWriter(responseBody);
            gson.toJson(responseBodyJson, writer);
            writer.flush();
            writer.close();
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

}
