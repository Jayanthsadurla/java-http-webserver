import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class SimpleWebServer {
    private static final Logger logger = Logger.getLogger(SimpleWebServer.class.getName());
    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;
    private static final String WEB_ROOT = "./www";
    
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    
    public SimpleWebServer() {
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        setupWebRoot();
    }
    
    private void setupWebRoot() {
        try {
            Files.createDirectories(Paths.get(WEB_ROOT));
            createDefaultIndexPage();
        } catch (IOException e) {
            logger.severe("Failed to create web root: " + e.getMessage());
        }
    }
    
    private void createDefaultIndexPage() throws IOException {
        Path indexPath = Paths.get(WEB_ROOT, "index.html");
        if (!Files.exists(indexPath)) {
            String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Simple Web Server</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial; margin: 40px; background: #f0f0f0; }\n" +
                "        .container { background: white; padding: 30px; border-radius: 8px; }\n" +
                "        h1 { color: #333; }\n" +
                "        .info { color: #666; margin-top: 20px; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>🚀 Multi-Threaded Web Server</h1>\n" +
                "        <p>Server is running successfully!</p>\n" +
                "        <div class=\"info\">\n" +
                "            <p><strong>Features:</strong></p>\n" +
                "            <ul>\n" +
                "                <li>Multi-threaded request handling</li>\n" +
                "                <li>Static file serving</li>\n" +
                "                <li>HTTP/1.1 protocol support</li>\n" +
                "                <li>Thread pool with 10 workers</li>\n" +
                "            </ul>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
            Files.writeString(indexPath, html);
        }
    }
    
    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        running = true;
        logger.info("Server started on port " + PORT);
        logger.info("Web root: " + new File(WEB_ROOT).getAbsolutePath());
        logger.info("Thread pool size: " + THREAD_POOL_SIZE);
        
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                logger.info("New connection from: " + clientSocket.getInetAddress());
                threadPool.execute(new RequestHandler(clientSocket));
            } catch (IOException e) {
                if (running) {
                    logger.warning("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            threadPool.shutdown();
            threadPool.awaitTermination(5, TimeUnit.SECONDS);
            logger.info("Server stopped");
        } catch (IOException | InterruptedException e) {
            logger.severe("Error stopping server: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        SimpleWebServer server = new SimpleWebServer();
        
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        
        try {
            logger.info("Starting server...");
            logger.info("Visit http://localhost:" + PORT);
            server.start();
        } catch (IOException e) {
            logger.severe("Failed to start server: " + e.getMessage());
            System.exit(1);
        }
    }
}