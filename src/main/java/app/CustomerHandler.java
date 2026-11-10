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

final class CustomerHandler extends ApiHandler {
    private final RentalService service;

    CustomerHandler(RentalService service) {
        this.service = service;
    }

    @Override
    protected void route(HttpExchange exchange) throws IOException {
        String[] path = exchange.getRequestURI().getPath().split("/", -1);
        if (path.length < 2 || !path[1].equals("customers")) {
            throw new HttpException(404, "Маршрут не найден");
        }
        if (path.length == 2) {
            collection(exchange);
        } else if (path.length == 3 && !path[2].isEmpty()) {
            resource(exchange, positiveId(path[2]));
        } else if (path.length == 4 && (path[3].equals("rentals") || path[3].equals("returns"))) {
            rental(exchange, path[2], path[3]);
        } else {
            throw new HttpException(404, "Маршрут не найден");
        }
    }

    private void collection(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
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
    }

    private void resource(HttpExchange exchange, long id) throws IOException {
        String method = exchange.getRequestMethod();
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
    }

    private void rental(HttpExchange exchange, String id, String action) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            methodNotAllowed(exchange, "POST");
        }
        long customerId = positiveId(id);
        query(exchange, List.of());
        JsonNode request = read(exchange, List.of("bikeId"));
        JsonNode value = request.get("bikeId");
        if (!value.isIntegralNumber() || !value.canConvertToLong() || value.longValue() <= 0) {
            throw new IllegalArgumentException("bikeId должен быть положительным целым числом");
        }
        Bike bike;
        if (action.equals("rentals")) {
            bike = service.rentBike(customerId, value.longValue());
        } else {
            bike = service.returnBike(customerId, value.longValue());
        }
        send(exchange, 200, bike);
    }
}
