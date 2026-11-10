package app;

import com.sun.net.httpserver.HttpExchange;
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

final class HttpSupport {
    private static final int MAX_BODY_SIZE = 16 * 1024;
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .build();

    private HttpSupport() {
    }

    static void methodNotAllowed(HttpExchange exchange, String allowed) {
        exchange.getResponseHeaders().set("Allow", allowed);
        throw new HttpException(405, "Метод не поддерживается");
    }

    static long positiveId(String value) {
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

    static Map<String, String> query(HttpExchange exchange, List<String> allowed) {
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

    static JsonNode read(HttpExchange exchange, List<String> fields) throws IOException {
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
        JsonNode request = MAPPER.readTree(body);
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

    static String textField(JsonNode request, String field) {
        JsonNode value = request.get(field);
        if (!value.isTextual()) {
            throw new IllegalArgumentException(field + " должен быть строкой");
        }
        return value.asText();
    }

    static void send(HttpExchange exchange, int status, Object value) throws IOException {
        if (status == 204) {
            exchange.sendResponseHeaders(status, -1);
            return;
        }
        byte[] body = MAPPER.writeValueAsBytes(value);
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
