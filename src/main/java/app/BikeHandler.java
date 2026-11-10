package app;

import com.sun.net.httpserver.HttpExchange;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static app.HttpSupport.methodNotAllowed;
import static app.HttpSupport.positiveId;
import static app.HttpSupport.query;
import static app.HttpSupport.read;
import static app.HttpSupport.send;
import static app.HttpSupport.textField;

final class BikeHandler extends ApiHandler {
    private final RentalService service;

    BikeHandler(RentalService service) {
        this.service = service;
    }

    @Override
    protected void route(HttpExchange exchange) throws IOException {
        String[] path = exchange.getRequestURI().getPath().split("/", -1);
        if (path.length < 2 || !path[1].equals("bikes")) {
            throw new HttpException(404, "Маршрут не найден");
        }
        if (path.length == 2) {
            collection(exchange);
        } else if (path.length == 3 && !path[2].isEmpty()) {
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
}
