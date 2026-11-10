package app;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

@RestControllerAdvice(assignableTypes = {BikeController.class, CustomerController.class})
class RequestBodyLimit extends RequestBodyAdviceAdapter {
    private static final int MAX_BODY_SIZE = 16 * 1024;

    @Override
    public boolean supports(MethodParameter parameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage input, MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        if (input.getHeaders().getContentLength() > MAX_BODY_SIZE) {
            throw tooLarge();
        }
        byte[] body = input.getBody().readNBytes(MAX_BODY_SIZE + 1);
        if (body.length > MAX_BODY_SIZE) {
            throw tooLarge();
        }
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(body);
            }

            @Override
            public HttpHeaders getHeaders() {
                return input.getHeaders();
            }
        };
    }

    private ResponseStatusException tooLarge() {
        return new ResponseStatusException(HttpStatus.CONTENT_TOO_LARGE, "Тело запроса превышает 16 КиБ");
    }
}
