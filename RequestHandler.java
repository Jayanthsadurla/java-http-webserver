import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

class RequestHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());
    private static final String WEB_ROOT = "./www";
    private static final Map<String, String> MIME_TYPES = Map.of(
        "html", "text/html",
        "css", "text/css",
        "js", "application/javascript",
        "jpg", "image/jpeg",
        "jpeg", "image/jpeg",
        "png", "image/png",
        "gif", "image/gif",
        "txt", "text/plain"
    );
    
    private final Socket clientSocket;
    
    public RequestHandler(Socket socket) {
        this.clientSocket = socket;
    }
    
    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
            );
            OutputStream out = clientSocket.getOutputStream()
        ) {
            HttpRequest request = parseRequest(in);
            if (request == null) {
                sendError(out, 400, "Bad Request");
                return;
            }
            
            logger.info(String.format("Thread %s: %s %s", 
                Thread.currentThread().getName(),
                request.method, 
                request.path
            ));
            
            handleRequest(request, out);
            
        } catch (IOException e) {
            logger.warning("Error handling request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.warning("Error closing socket: " + e.getMessage());
            }
        }
    }
    
    private HttpRequest parseRequest(BufferedReader in) throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }
        
        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            return null;
        }
        
        HttpRequest request = new HttpRequest();
        request.method = parts[0];
        request.path = parts[1];
        request.version = parts[2];
        
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                request.headers.put(key, value);
            }
        }
        
        return request;
    }
    
    private void handleRequest(HttpRequest request, OutputStream out) throws IOException {
        if (!request.method.equals("GET")) {
            sendError(out, 405, "Method Not Allowed");
            return;
        }
        
        String filePath = request.path.equals("/") ? "/index.html" : request.path;
        Path fullPath = Paths.get(WEB_ROOT + filePath).normalize();
        
        if (!fullPath.startsWith(Paths.get(WEB_ROOT).normalize())) {
            sendError(out, 403, "Forbidden");
            return;
        }
        
        if (!Files.exists(fullPath) || Files.isDirectory(fullPath)) {
            sendError(out, 404, "Not Found");
            return;
        }
        
        serveFile(fullPath, out);
    }
    
    private void serveFile(Path filePath, OutputStream out) throws IOException {
        byte[] fileContent = Files.readAllBytes(filePath);
        String contentType = getContentType(filePath.toString());
        
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 200 OK\r\n");
        response.append("Content-Type: ").append(contentType).append("\r\n");
        response.append("Content-Length: ").append(fileContent.length).append("\r\n");
        response.append("Connection: close\r\n");
        response.append("\r\n");
        
        out.write(response.toString().getBytes());
        out.write(fileContent);
        out.flush();
        
        logger.info("Served: " + filePath.getFileName() + " (" + fileContent.length + " bytes)");
    }
    
    private void sendError(OutputStream out, int statusCode, String message) throws IOException {
        String body = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head><title>" + statusCode + " " + message + "</title></head>\n" +
            "<body>\n" +
            "    <h1>" + statusCode + " " + message + "</h1>\n" +
            "    <p>The requested resource could not be found.</p>\n" +
            "</body>\n" +
            "</html>";
        
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(message).append("\r\n");
        response.append("Content-Type: text/html\r\n");
        response.append("Content-Length: ").append(body.length()).append("\r\n");
        response.append("Connection: close\r\n");
        response.append("\r\n");
        response.append(body);
        
        out.write(response.toString().getBytes());
        out.flush();
    }
    
    private String getContentType(String filePath) {
        String extension = "";
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot > 0) {
            extension = filePath.substring(lastDot + 1).toLowerCase();
        }
        return MIME_TYPES.getOrDefault(extension, "application/octet-stream");
    }
    
    static class HttpRequest {
        String method;
        String path;
        String version;
        Map<String, String> headers = new HashMap<>();
    }
}