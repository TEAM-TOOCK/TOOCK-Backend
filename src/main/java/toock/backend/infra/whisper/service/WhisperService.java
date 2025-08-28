package toock.backend.infra.whisper.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhisperService {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${whisper.upload.dir:uploads/audio}")
    private String uploadDir;

    /**
     * 음성 파일을 텍스트로 변환
     * @param audioFile 업로드된 음성 파일
     * @return 변환된 텍스트
     */
    public String transcribeAudio(MultipartFile audioFile) {
        try {
            validateAudioFile(audioFile);
            
            Path tempFilePath = saveToTempFile(audioFile);
            
            try {
                String transcription = callWhisperApi(tempFilePath, audioFile.getOriginalFilename());
                log.info("음성 변환 완료: 파일명={}, 변환된 텍스트 길이={}", 
                    audioFile.getOriginalFilename(), 
                    transcription.length());
                return transcription;
            } finally {
                Files.deleteIfExists(tempFilePath);
            }
            
        } catch (Exception e) {
            log.error("음성 변환 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("음성 변환 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * OpenAI Whisper API 호출
     */
    private String callWhisperApi(Path audioFilePath, String originalFilename) throws IOException {
        try {
            byte[] audioBytes = Files.readAllBytes(audioFilePath);
            ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
                @Override
                public String getFilename() {
                    return originalFilename;
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", audioResource);
            body.add("model", "whisper-1");

            String response = webClient.post()
                    .uri("https://api.openai.com/v1/audio/transcriptions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // JSON 응답에서 text 필드 추출
            return extractTextFromResponse(response);
            
        } catch (Exception e) {
            log.error("Whisper API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("Whisper API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * API 응답에서 텍스트 추출
     */
    private String extractTextFromResponse(String response) {
        // TODO: Jackson으로 직렬화 하는게 더 좋을듯?
        if (response != null && response.contains("\"text\"")) {
            int textStart = response.indexOf("\"text\"") + 8;
            int textEnd = response.indexOf("\"", textStart);
            if (textEnd > textStart) {
                return response.substring(textStart, textEnd);
            }
        }
        throw new RuntimeException("API 응답에서 텍스트를 추출할 수 없습니다.");
    }

    /**
     * 음성 파일 유효성 검사
     */
    private void validateAudioFile(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("음성 파일이 비어있습니다.");
        }

        String originalFilename = audioFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("유효하지 않은 파일명입니다.");
        }

        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        if (!isSupportedAudioFormat(fileExtension)) {
            throw new IllegalArgumentException("지원하지 않는 오디오 형식입니다. 지원 형식: mp3, mp4, mpeg, mpga, m4a, wav, webm");
        }

        long maxSize = 25 * 1024 * 1024; // 25MB
        if (audioFile.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기가 너무 큽니다. 최대 25MB까지 지원합니다.");
        }
    }

    /**
     * 임시 파일로 저장
     */
    private Path saveToTempFile(MultipartFile audioFile) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = audioFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(fileExtension);
        
        Path filePath = uploadPath.resolve(uniqueFilename);
        
        Files.copy(audioFile.getInputStream(), filePath);
        
        return filePath;
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    /**
     * 지원하는 오디오 형식인지 확인
     */
    private boolean isSupportedAudioFormat(String extension) {
        String[] supportedFormats = {"mp3", "mp4", "mpeg", "mpga", "m4a", "wav", "webm"};
        for (String format : supportedFormats) {
            if (format.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 고유한 파일명 생성
     */
    private String generateUniqueFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("audio_%s_%s.%s", timestamp, uuid, extension);
    }
}
