package app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiHandler implements HttpHandler {
    private static final int MAX_BODY_SIZE = 16 * 1024;
    private final RentalService service;
    private final JsonMapper mapper = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .build();

    public ApiHandler(RentalService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            route(exchange);
        } catch (NotFoundException error) {
            send(exchange, 404, Map.of("error", error.getMessage()));
        } catch (HttpException error) {
            send(exchange, error.getStatus(), Map.of("error", error.getMessage()));
        } catch (JacksonException error) {
            send(exchange, 400, Map.of("error", "Некорректный JSON или типы полей"));
        } catch (IllegalArgumentException error) {
            send(exchange, 400, Map.of("error", error.getMessage()));
        } catch (IllegalStateException error) {
            send(exchange, 409, Map.of("error", error.getMessage()));
        } catch (IOException error) {
            System.err.println("Ошибка соединения: " + error.getMessage());
        } catch (Exception error) {
            error.printStackTrace();
            send(exchange, 500, Map.of("error", "Внутренняя ошибка сервера"));
        } finally {
            exchange.close();
        }
    }

    private void route(HttpExchange exchange) throws IOException {
        String[] path = exchange.getRequestURI().getPath().split("/", -1);
        if (path.length >= 2 && path[1].equals("customers")) {
            customers(exchange, path);
            return;
        }
        if (path.length == 2 && path[1].equals("bikes")) {
            collection(exchange);
        } else if (path.length == 3 && path[1].equals("bikes") && !path[2].isEmpty()) {
            resource(exchange, positiveId(path[2]));
        } else {
            throw new HttpException(404, "Маршрут не найден");
        }
    }

    private void collection(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (method.equals("GET")) {
            Map<String, String> params = query(exchange, List.of("model", "available"));
            Boolean available = null;
            if (params.containsKey("available")) {
                String value = params.get("available");
                if (!value.equals("true") && !value.equals("false")) {
                    throw new IllegalArgumentException("available должен быть true или false");
                }
                available = Boolean.valueOf(value);
            }
            send(exchange, 200, service.getBikes(params.get("model"), available));
        } else if (method.equals("POST")) {
            query(exchange, List.of());
            JsonNode request = read(exchange, List.of("model"));
            Bike bike = service.createBike(textField(request, "model"));
            exchange.getResponseHeaders().set("Location", "/bikes/" + bike.getId());
            send(exchange, 201, bike);
        } else {
            methodNotAllowed(exchange, "GET, POST");
        }
    }

    private void resource(HttpExchange exchange, long id) throws IOException {
        query(exchange, List.of());
        String method = exchange.getRequestMethod();
        if (method.equals("GET")) {
            send(exchange, 200, service.getBike(id));
        } else if (method.equals("PUT")) {
            JsonNode request = read(exchange, List.of("model"));
            send(exchange, 200, service.updateBike(id, textField(request, "model")));
        } else if (method.equals("DELETE")) {
            service.deleteBike(id);
            send(exchange, 204, null);
        } else {
            methodNotAllowed(exchange, "GET, PUT, DELETE");
        }
    }

    private void customers(HttpExchange exchange, String[] path) throws IOException {
        String method = exchange.getRequestMethod();
        if (path.length == 2) {
            if (method.equals("GET")) {
                Map<String, String> params = query(exchange, List.of("name", "type"));
                send(exchange, 200, service.getCustomers(params.get("name"), params.get("type")));
            } else if (method.equals("POST")) {
                query(exchange, List.of());
                JsonNode request = read(exchange, List.of("name", "type"));
                Customer customer = service.createCustomer(textField(request, "name"), textField(request, "type"));
                exchange.getResponseHeaders().set("Location", "/customers/" + customer.getId());
                send(exchange, 201, customer);
            } else {
                methodNotAllowed(exchange, "GET, POST");
            }
        } else if (path.length == 3 && !path[2].isEmpty()) {
            long id = positiveId(path[2]);
            query(exchange, List.of());
            if (method.equals("GET")) {
                send(exchange, 200, service.getCustomer(id));
            } else if (method.equals("PUT")) {
                JsonNode request = read(exchange, List.of("name", "type"));
                send(exchange, 200, service.updateCustomer(id, textField(request, "name"), textField(request, "type")));
            } else if (method.equals("DELETE")) {
                service.deleteCustomer(id);
                send(exchange, 204, null);
            } else {
                methodNotAllowed(exchange, "GET, PUT, DELETE");
            }
        } else if (path.length == 4 && (path[3].equals("rentals") || path[3].equals("returns"))) {
            if (!method.equals("POST")) {
                methodNotAllowed(exchange, "POST");
            }
            long customerId = positiveId(path[2]);
            query(exchange, List.of());
            JsonNode request = read(exchange, List.of("bikeId"));
            JsonNode value = request.get("bikeId");
            if (!value.isIntegralNumber() || !value.canConvertToLong() || value.longValue() <= 0) {
                throw new IllegalArgumentException("bikeId должен быть положительным целым числом");
            }
            Bike bike;
            if (path[3].equals("rentals")) {
                bike = service.rentBike(customerId, value.longValue());
            } else {
                bike = service.returnBike(customerId, value.longValue());
            }
            send(exchange, 200, bike);
        } else {
            throw new HttpException(404, "Маршрут не найден");
        }
    }

    private void methodNotAllowed(HttpExchange exchange, String allowed) {
        exchange.getResponseHeaders().set("Allow", allowed);
        throw new HttpException(405, "Метод не поддерживается");
    }

    private long positiveId(String value) {
        long id;
        try {
            id = Long.parseLong(value);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("id должен быть положительным целым числом");
        }
        if (id <= 0) {
            throw new IllegalArgumentException("id должен быть положительным целым числом");
        }
        return id;
    }

    private Map<String, String> query(HttpExchange exchange, List<String> allowed) {
        Map<String, String> result = new HashMap<>();
        String raw = exchange.getRequestURI().getRawQuery();
        if (raw == null || raw.isEmpty()) {
            return result;
        }
        for (String part : raw.split("&", -1)) {
            String[] pair = part.split("=", 2);
            if (pair.length != 2) {
                throw new IllegalArgumentException("Параметр должен иметь вид имя=значение");
            }
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            if (!allowed.contains(key) || result.containsKey(key) || value.isBlank()) {
                throw new IllegalArgumentException("Неизвестный, пустой или повторяющийся параметр: " + key);
            }
            result.put(key, value);
        }
        return result;
    }

    private JsonNode read(HttpExchange exchange, List<String> fields) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.split(";", 2)[0].trim().equalsIgnoreCase("application/json")) {
            throw new HttpException(415, "Нужен Content-Type: application/json");
        }
        byte[] body = exchange.getRequestBody().readNBytes(MAX_BODY_SIZE + 1);
        if (body.length > MAX_BODY_SIZE) {
            throw new HttpException(413, "Тело запроса превышает 16 КиБ");
        }
        if (body.length == 0) {
            throw new IllegalArgumentException("Пустое тело запроса");
        }
        JsonNode request = mapper.readTree(body);
        if (request == null || !request.isObject()) {
            throw new IllegalArgumentException("В теле нужен JSON-объект");
        }
        if (request.size() != fields.size()) {
            throw new IllegalArgumentException("Нужны только поля: " + fields);
        }
        for (String field : fields) {
            if (!request.has(field)) {
                throw new IllegalArgumentException("Отсутствует поле: " + field);
            }
        }
        return request;
    }

    private String textField(JsonNode request, String field) {
        JsonNode value = request.get(field);
        if (!value.isTextual()) {
            throw new IllegalArgumentException(field + " должен быть строкой");
        }
        return value.asText();
    }

    private void send(HttpExchange exchange, int status, Object value) throws IOException {
        if (status == 204) {
            exchange.sendResponseHeaders(status, -1);
            return;
        }
        byte[] body = mapper.writeValueAsBytes(value);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        if (exchange.getRequestMethod().equals("HEAD")) {
            exchange.sendResponseHeaders(status, -1);
            return;
        }
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
