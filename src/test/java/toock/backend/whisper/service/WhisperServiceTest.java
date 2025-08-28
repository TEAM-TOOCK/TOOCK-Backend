package toock.backend.whisper.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import toock.backend.infra.whisper.service.WhisperService;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WhisperServiceTest {

    @Mock
    private WebClient webClient;

    @InjectMocks
    private WhisperService whisperService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(whisperService, "uploadDir", "test-uploads");
    }

    @Test
    void testTranscribeAudio_EmptyFile() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
            "audioFile",
            "empty.mp3",
            "audio/mpeg",
            new byte[0]
        );

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            whisperService.transcribeAudio(emptyFile);
        });
    }

    @Test
    void testTranscribeAudio_UnsupportedFormat() {
        // Given
        MockMultipartFile unsupportedFile = new MockMultipartFile(
            "audioFile",
            "test.txt",
            "text/plain",
            "test content".getBytes()
        );

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            whisperService.transcribeAudio(unsupportedFile);
        });
    }

    @Test
    void testTranscribeAudio_NullFile() {
        // When & Then
        assertThrows(RuntimeException.class, () -> {
            whisperService.transcribeAudio(null);
        });
    }
}
