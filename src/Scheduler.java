import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;


public class Scheduler {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // API endpoint
        server.createContext("/api/schedule", new ScheduleHandler());

        // Serve frontend files
        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("==============================================");
        System.out.println("  CPU Scheduler Server running on port " + port);
        System.out.println("  Open: http://localhost:" + port);
        System.out.println("==============================================");
    }

    // ──────────────────────────────────────────────
    //  Tiny static file server for the frontend
    // ──────────────────────────────────────────────
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().getPath();
            if (uri.equals("/")) uri = "/index.html";

            // Look for files in the 'frontend' subfolder next to the bin/ directory
            String base = System.getProperty("user.dir") + File.separator + "frontend";
            File file = new File(base + uri);

            if (!file.exists() || file.isDirectory()) {
                String body = "404 Not Found";
                exchange.sendResponseHeaders(404, body.length());
                exchange.getResponseBody().write(body.getBytes());
                exchange.getResponseBody().close();
                return;
            }

            String ct = "text/html";
            if (uri.endsWith(".css"))  ct = "text/css";
            if (uri.endsWith(".js"))   ct = "application/javascript";
            if (uri.endsWith(".ico"))  ct = "image/x-icon";

            byte[] bytes;
            try (FileInputStream fis = new FileInputStream(file)) {
                bytes = fis.readAllBytes();
            }
            exchange.getResponseHeaders().set("Content-Type", ct);
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(bytes);
            }
        }
    }
}
