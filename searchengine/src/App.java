import server.Server;

public class App {
    public App() throws Exception {
        new Server(8000);
    }

    public static void main(String[] args) throws Exception {
        new App();
    }
}
