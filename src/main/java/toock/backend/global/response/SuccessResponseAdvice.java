package toock.backend.global.response;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import com.example.runway.global.response.ApiResponse;

@Slf4j
@RestControllerAdvice
public class SuccessResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        // 이미 래핑된 응답은 스킵
        if (body instanceof ApiResponse<?>) return body;


        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int statusCode = servletResponse.getStatus();
        HttpStatus httpStatus = HttpStatus.resolve(statusCode);

        //성공 코드일 경우(실패는 따로 처리)
        if (httpStatus != null && httpStatus.is2xxSuccessful()) {
            // 204면 본문을 쓰지 않음 (No Content)
            if (statusCode == HttpStatus.NO_CONTENT.value()) {
                return null;
            }

            // 상태코드가 기본값(200)으로만 들어오는 환경 대비: 메서드로 보정 (POST=201/DELETE=204/기타=200)
            if (statusCode == 200) {
                String method = ((ServletServerHttpRequest) request).getServletRequest().getMethod();
                statusCode = statusProvider(method);
                // 실 응답코드도 맞춰줌
                servletResponse.setStatus(statusCode);
            }


            return ApiResponse.success(statusCode, body, "요청에 성공하였습니다.");
        }

        // 비-2xx는 이 Advice에서 건드리지 않고 ExceptionHandler에서 처리하도록 둠
        return body;
    }

    private int statusProvider(String method) {
        return switch (method) {
            case "POST" -> 201;
            case "DELETE" -> 204;
            default -> 200;
        };
    }
}