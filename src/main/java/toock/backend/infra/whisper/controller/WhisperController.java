package toock.backend.infra.whisper.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import toock.backend.infra.whisper.dto.TranscriptionResponseDto;
import toock.backend.infra.whisper.service.WhisperService;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/whisper")
@RequiredArgsConstructor
public class WhisperController {

    private final WhisperService whisperService;

    /**
     * 음성 파일을 텍스트로 변환
     * @param audioFile 업로드된 음성 파일
     * @return 변환된 텍스트와 메타데이터
     */
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TranscriptionResponseDto> transcribeAudio(
            @RequestParam("audioFile") MultipartFile audioFile) {
        
        try {
            log.info("음성 변환 요청 수신: 파일명={}, 크기={} bytes", 
                audioFile.getOriginalFilename(), audioFile.getSize());

            // 음성 변환 수행
            String transcription = whisperService.transcribeAudio(audioFile);

            // 응답 DTO 생성
            TranscriptionResponseDto response = TranscriptionResponseDto.builder()
                    .transcription(transcription)
                    .originalFilename(audioFile.getOriginalFilename())
                    .fileSize(audioFile.getSize())
                    .processedAt(LocalDateTime.now())
                    .status("SUCCESS")
                    .message("음성 변환이 성공적으로 완료되었습니다.")
                    .build();

            log.info("음성 변환 완료: 파일명={}, 변환된 텍스트 길이={}", 
                audioFile.getOriginalFilename(), transcription.length());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("잘못된 요청: {}", e.getMessage());
            
            TranscriptionResponseDto errorResponse = TranscriptionResponseDto.builder()
                    .status("ERROR")
                    .message(e.getMessage())
                    .processedAt(LocalDateTime.now())
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("음성 변환 처리 중 오류 발생: {}", e.getMessage(), e);
            
            TranscriptionResponseDto errorResponse = TranscriptionResponseDto.builder()
                    .status("ERROR")
                    .message("음성 변환 처리 중 오류가 발생했습니다.")
                    .processedAt(LocalDateTime.now())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 서비스 상태 확인
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Whisper 서비스가 정상적으로 실행 중입니다.");
    }
}
