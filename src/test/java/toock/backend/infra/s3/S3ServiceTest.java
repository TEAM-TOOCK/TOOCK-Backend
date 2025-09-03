package toock.backend.infra.s3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Util s3Util;

    @InjectMocks
    private S3Service s3Service;

    @Test
    @DisplayName("파일 업로드 성공 시 URL을 반환한다")
    void uploadImage_success_returnsUrl() throws IOException {
        // Given
        String folderName = "test-audios";
        String uniqueFilename = "test-audios/unique-id.mp3";
        MultipartFile file = new MockMultipartFile("audioFile", "test-audio.mp3", "audio/mpeg", "content".getBytes());

        // Mocking behavior
        when(s3Util.uploadFile(anyString(), any(MultipartFile.class)))
                .thenReturn("https://toock-test-bucket.s3.ap-northeast-2.amazonaws.com/" + uniqueFilename);

        // When
        String uploadedUrl = s3Service.uploadAudio(file, folderName);

        // Then
        assertThat(uploadedUrl).contains(folderName);
        assertThat(uploadedUrl).contains("http");

        // s3Util의 uploadFile 메서드가 정확히 한 번 호출되었는지 검증
        verify(s3Util, times(1)).uploadFile(anyString(), any(MultipartFile.class));
    }

    @Test
    @DisplayName("파일 삭제가 성공한다")
    void removeImage_success() {
        // Given
        String fileUrl = "https://toock-test-bucket.s3.ap-northeast-2.amazonaws.com/test-audios/unique-id.mp3";

        // Mocking behavior
        doNothing().when(s3Util).deleteFile(fileUrl);

        // When
        s3Service.removeAudio(fileUrl);

        // Then
        // s3Util의 deleteFile 메서드가 정확히 한 번 호출되었는지 검증
        verify(s3Util, times(1)).deleteFile(fileUrl);
    }
}