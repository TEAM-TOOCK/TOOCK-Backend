package toock.backend.infra.whisper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionResponseDto {
    private String transcription;
    private String originalFilename;
    private long fileSize;
    private LocalDateTime processedAt;
    private String status;
    private String message;
}
