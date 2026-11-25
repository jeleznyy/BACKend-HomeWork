package app;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.HashSet;
import java.util.Set;

@Configuration
class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new QueryParameters())
                .addPathPatterns("/bikes", "/bikes/**", "/customers", "/customers/**");
    }

    static class QueryParameters implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            if (!(handler instanceof HandlerMethod method) || request.getParameterMap().isEmpty()) {
                return true;
            }
            Set<String> allowed = new HashSet<>();
            for (var parameter : method.getMethodParameters()) {
                RequestParam annotation = parameter.getParameterAnnotation(RequestParam.class);
                if (annotation != null) {
                    allowed.add(annotation.name());
                }
            }
            for (var entry : request.getParameterMap().entrySet()) {
                String[] values = entry.getValue();
                if (!allowed.contains(entry.getKey()) || values.length != 1 || values[0].isBlank()) {
                    throw new IllegalArgumentException(
                            "Неизвестный, пустой или повторяющийся параметр: " + entry.getKey());
                }
            }
            return true;
        }
    }
}
