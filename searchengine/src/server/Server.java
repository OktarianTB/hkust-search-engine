package server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.sun.net.httpserver.HttpServer;

// this class is the entry point for the server
public class Server {
    public Server(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        server.setExecutor(threadPoolExecutor);

        server.createContext("/search", new SearchHandler());
        server.createContext("/similar-documents", new SimilarDocumentsHandler());
        server.start();

        System.out.println("Server started on port " + port);
    }
}
