package toock.backend.global.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import toock.backend.global.dto.CommonResponseDto;
import toock.backend.infra.whisper.exception.WhisperException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    // 테스트를 위한 더미 컨트롤러
    @RestController
    @RequestMapping("/test")
    static class TestController {
        @GetMapping("/illegal-argument")
        public String throwIllegalArgument() {
            throw new IllegalArgumentException(ErrorCode.INVALID_INPUT_VALUE.getMessage());
        }

        @GetMapping("/whisper-exception")
        public String throwWhisperException() {
            throw new WhisperException(ErrorCode.WHISPER_TRANSCRIBE_FAILED, ErrorCode.WHISPER_TRANSCRIBE_FAILED.getMessage());
        }

        @PostMapping("/method-not-allowed") // GET 요청 시 HttpRequestMethodNotSupportedException 발생 유도
        public String methodNotAllowed() {
            return "OK";
        }

        @PostMapping(value = "/unsupported-media-type", consumes = MediaType.APPLICATION_XML_VALUE)
        public String unsupportedMediaType(@RequestBody String body) {
            return "OK";
        }

        @PostMapping("/validation")
        public String validation(@RequestBody @Valid TestDto dto) { // MethodArgumentNotValidException 발생 유도
            return "OK";
        }

        @GetMapping("/unhandled")
        public String throwUnhandledException() throws Exception {
            throw new Exception(ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    // MethodArgumentNotValidException 테스트를 위한 더미 DTO
    static class TestDto {
        @NotBlank
        String name;

        @Override
        public String toString() {
            return "TestDto{name='" + name + "'}";
        }
    }

    @Test
    @DisplayName("IllegalArgumentException 처리")
    void handleIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.name()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }

    @Test
    @DisplayName("WhisperException 처리")
    void handleWhisperException() throws Exception {
        mockMvc.perform(get("/test/whisper-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.WHISPER_TRANSCRIBE_FAILED.name()))
                .andExpect(jsonPath("$.message").value(ErrorCode.WHISPER_TRANSCRIBE_FAILED.getMessage()));
    }

    @Test
    @DisplayName("HttpRequestMethodNotSupportedException 처리")
    void handleHttpRequestMethodNotSupportedException() throws Exception {
        mockMvc.perform(get("/test/method-not-allowed")) // POST 엔드포인트에 GET 요청
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.METHOD_NOT_ALLOWED.name()))
                .andExpect(jsonPath("$.message").value(ErrorCode.METHOD_NOT_ALLOWED.getMessage()));
    }

    @Test
    @DisplayName("HttpMediaTypeNotSupportedException 처리")
    void handleHttpMediaTypeNotSupportedException() throws Exception {
        mockMvc.perform(post("/test/unsupported-media-type")
                        .contentType(MediaType.APPLICATION_JSON) // XML만 지원하는 엔드포인트에 JSON 요청
                        .content("{}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.UNSUPPORTED_MEDIA_TYPE.name()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getMessage()));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 처리")
    void handleMethodArgumentNotValidException() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": null }")) // @NotBlank 필드가 없으므로 유효성 검사 실패
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_INPUT_VALUE.name()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }

    @Test
    @DisplayName("Unhandled Exception 처리")
    void handleUnhandledException() throws Exception {
        mockMvc.perform(get("/test/unhandled"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(ErrorCode.INTERNAL_SERVER_ERROR.name()))
                .andExpect(jsonPath("$.message").value(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
