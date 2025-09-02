package toock.backend.infra.whisper.exception;

import toock.backend.global.error.ErrorCode;

public class WhisperException extends RuntimeException {
    private final ErrorCode errorCode;

    public WhisperException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public WhisperException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
