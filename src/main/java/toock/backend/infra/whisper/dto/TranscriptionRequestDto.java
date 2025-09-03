package toock.backend.infra.whisper.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TranscriptionRequestDto {
    private MultipartFile audioFile;
}
