package app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.jackson.core.JacksonException;

import java.io.IOException;
import java.util.Map;

import static app.HttpSupport.send;

abstract class ApiHandler implements HttpHandler {
    @Override
    public final void handle(HttpExchange exchange) throws IOException {
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

    protected abstract void route(HttpExchange exchange) throws IOException;
}
