package toock.backend.global.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import toock.backend.global.dto.CommonResponseDto;
import toock.backend.infra.whisper.exception.WhisperException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponseDto<Void>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Invalid input: {}", ex.getMessage());
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
                .body(CommonResponseDto.fail(ErrorCode.INVALID_INPUT_VALUE.name(), ErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }

    @ExceptionHandler(WhisperException.class)
    public ResponseEntity<CommonResponseDto<Void>> handleWhisper(WhisperException ex, HttpServletRequest request) {
        ErrorCode code = ex.getErrorCode() != null ? ex.getErrorCode() : ErrorCode.WHISPER_TRANSCRIBE_FAILED;
        log.error("Whisper error({}): {}", code.name(), ex.getMessage(), ex);
        return ResponseEntity.status(code.getHttpStatus())
                .body(CommonResponseDto.fail(code.name(), code.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CommonResponseDto<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus())
                .body(CommonResponseDto.fail(ErrorCode.METHOD_NOT_ALLOWED.name(), ErrorCode.METHOD_NOT_ALLOWED.getMessage()));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<CommonResponseDto<Void>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        return ResponseEntity.status(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getHttpStatus())
                .body(CommonResponseDto.fail(ErrorCode.UNSUPPORTED_MEDIA_TYPE.name(), ErrorCode.UNSUPPORTED_MEDIA_TYPE.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponseDto<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse(ErrorCode.INVALID_INPUT_VALUE.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponseDto.fail(ErrorCode.INVALID_INPUT_VALUE.name(), ErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponseDto<Void>> handleUnhandled(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(CommonResponseDto.fail(ErrorCode.INTERNAL_SERVER_ERROR.name(), ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
