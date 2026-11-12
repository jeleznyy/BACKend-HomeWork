package app;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@RestControllerAdvice
class ApiErrors extends ResponseEntityExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<Map<String, String>> notFound(NotFoundException error) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", error.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("error", error.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String, String>> conflict(IllegalStateException error) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", error.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception error, Object body, HttpHeaders headers,
                                                             HttpStatusCode status, WebRequest request) {
        String message = switch (status.value()) {
            case 400 -> "Некорректный JSON или типы полей";
            case 404 -> "Маршрут не найден";
            case 405 -> "Метод не поддерживается";
            case 415 -> "Нужен Content-Type: application/json";
            default -> "Ошибка обработки запроса";
        };
        if (error instanceof MethodArgumentNotValidException invalid) {
            FieldError field = invalid.getBindingResult().getFieldError();
            if (field != null) {
                message = field.getField() + ": " + field.getDefaultMessage();
            }
        } else if (error instanceof ResponseStatusException statusError && statusError.getReason() != null) {
            message = statusError.getReason();
        }
        return super.handleExceptionInternal(error, Map.of("error", message), headers, status, request);
    }
}
