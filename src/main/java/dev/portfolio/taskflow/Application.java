package dev.portfolio.taskflow;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/** HTTP adapter using the JDK server; no external runtime dependencies. */
public final class Application {
    public static void main(String[] args) throws IOException {
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        TaskService service = new TaskService();
        server.createContext("/tasks", exchange -> handle(exchange, service));
        server.setExecutor(Executors.newFixedThreadPool(4));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(1)));
        server.start();
        System.out.println("TaskFlow listening on http://localhost:" + port);
    }

    private static void handle(HttpExchange exchange, TaskService service) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            if (path.equals("/tasks") && method.equals("GET")) {
                send(exchange, 200, service.list().stream().map(Application::json)
                        .collect(Collectors.joining(",", "[", "]")));
            } else if (path.equals("/tasks") && method.equals("POST")) {
                String query = exchange.getRequestURI().getRawQuery();
                String title = null;
                if (query != null) for (String part : query.split("&")) {
                    String[] pair = part.split("=", 2);
                    if (pair[0].equals("title") && pair.length == 2)
                        title = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                }
                TaskService.Task task = service.create(title);
                exchange.getResponseHeaders().set("Location", "/tasks/" + task.id());
                send(exchange, 201, json(task));
            } else if (path.matches("/tasks/[0-9]+/advance") && method.equals("POST")) {
                send(exchange, 200, json(service.advance(Long.parseLong(path.split("/")[2]))));
            } else if (path.equals("/tasks") || path.matches("/tasks/[0-9]+/advance")) {
                exchange.getResponseHeaders().set("Allow", path.equals("/tasks") ? "GET, POST" : "POST");
                send(exchange, 405, error("Method not allowed"));
            } else send(exchange, 404, error("Route not found"));
        } catch (IllegalArgumentException exception) {
            send(exchange, 400, error(exception.getMessage()));
        } catch (NoSuchElementException exception) {
            send(exchange, 404, error(exception.getMessage()));
        } catch (IllegalStateException exception) {
            send(exchange, 409, error(exception.getMessage()));
        } finally { exchange.close(); }
    }

    static String quote(String value) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            if (c == '"' || c == '\\') out.append('\\').append(c);
            else if (c < 32) out.append(String.format("\\u%04x", (int) c));
            else out.append(c);
        }
        return out.append('"').toString();
    }
    private static String json(TaskService.Task task) {
        return "{\"id\":" + task.id() + ",\"title\":" + quote(task.title())
                + ",\"status\":" + quote(task.status().name()) + "}";
    }
    private static String error(String message) { return "{\"error\":" + quote(message) + "}"; }
    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
