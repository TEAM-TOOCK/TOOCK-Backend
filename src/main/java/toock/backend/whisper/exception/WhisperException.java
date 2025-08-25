package toock.backend.whisper.exception;

public class WhisperException extends RuntimeException {
    
    public WhisperException(String message) {
        super(message);
    }
    
    public WhisperException(String message, Throwable cause) {
        super(message, cause);
    }
}
