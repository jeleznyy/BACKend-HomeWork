package app;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class RequestParameters {
    private RequestParameters() {
    }

    static Map<String, String> query(HttpServletRequest request, String... allowedNames) {
        Map<String, String> params = new HashMap<>();
        String raw = request.getQueryString();
        if (raw == null || raw.isEmpty()) {
            return params;
        }
        List<String> allowed = List.of(allowedNames);
        for (String part : raw.split("&", -1)) {
            String[] pair = part.split("=", 2);
            if (pair.length != 2) {
                throw new IllegalArgumentException("Параметр должен иметь вид имя=значение");
            }
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            if (!allowed.contains(key) || params.containsKey(key) || value.isBlank()) {
                throw new IllegalArgumentException("Неизвестный, пустой или повторяющийся параметр: " + key);
            }
            params.put(key, value);
        }
        return params;
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
}
