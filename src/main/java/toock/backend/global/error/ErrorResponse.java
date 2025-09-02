package toock.backend.global.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
public class ErrorResponse {
    private final int status;
    private final String error;
    private final String code;
    private final String message;
    private final String path;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;

    public static ErrorResponse of(ErrorCode errorCode, String path) {
        HttpStatus httpStatus = errorCode.getHttpStatus();
        return ErrorResponse.builder()
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .code(status.name())
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
