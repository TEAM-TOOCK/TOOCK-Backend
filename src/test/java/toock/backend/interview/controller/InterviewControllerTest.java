package toock.backend.interview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import toock.backend.infra.s3.S3Service;
import toock.backend.infra.whisper.service.WhisperService;
import toock.backend.interview.dto.InterviewAnalysisResponseDto;
import toock.backend.interview.service.InterviewService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public InterviewService interviewService() {
            return mock(InterviewService.class);
        }

        @Bean
        public S3Service s3Service() {
            return mock(S3Service.class);
        }

        @Bean
        public WhisperService whisperService() {
            return mock(WhisperService.class);
        }
        
    }

    @Autowired
    private InterviewService interviewService;
    @Autowired
    private S3Service s3Service;
    @Autowired
    private WhisperService whisperService;

    @Test
    @DisplayName("면접 평가 엔드포인트 성공")
    void analyzeInterview_Success() throws Exception {
        Long sessionId = 1L;
        InterviewAnalysisResponseDto mockResponseDto = InterviewAnalysisResponseDto.builder()
                .id(1L)
                .interviewSessionId(sessionId)
                .score(4)
                .technicalExpertiseScore(5)
                .collaborationCommunicationScore(4)
                .problemSolvingScore(4)
                .growthPotentialScore(3)
                .summary("좋은 요약")
                .build();

        when(interviewService.evaluateInterview(anyLong())).thenReturn(mockResponseDto);

        mockMvc.perform(post("/interviews/analyze/{interviewSessionId}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.interviewSessionId").value(sessionId))
                .andExpect(jsonPath("$.score").value(4))
                .andExpect(jsonPath("$.technicalExpertiseScore").value(5))
                .andExpect(jsonPath("$.collaborationCommunicationScore").value(4))
                .andExpect(jsonPath("$.problemSolvingScore").value(4))
                .andExpect(jsonPath("$.growthPotentialScore").value(3))
                .andExpect(jsonPath("$.summary").value("좋은 요약"));
    }

    @Test
    @DisplayName("면접 결과 조회 엔드포인트 성공")
    void getInterviewResult_Success() throws Exception {
        Long sessionId = 1L;
        InterviewAnalysisResponseDto mockResponseDto = InterviewAnalysisResponseDto.builder()
                .id(1L)
                .interviewSessionId(sessionId)
                .score(4)
                .technicalExpertiseScore(5)
                .collaborationCommunicationScore(4)
                .problemSolvingScore(4)
                .growthPotentialScore(3)
                .summary("좋은 요약")
                .build();

        when(interviewService.getInterviewAnalysis(anyLong())).thenReturn(mockResponseDto);

        mockMvc.perform(get("/interviews/results/{interviewSessionId}", sessionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("요청에 성공하였습니다."))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.interviewSessionId").value(sessionId))
                .andExpect(jsonPath("$.data.score").value(4))
                .andExpect(jsonPath("$.data.technicalExpertiseScore").value(5))
                .andExpect(jsonPath("$.data.collaborationCommunicationScore").value(4))
                .andExpect(jsonPath("$.data.problemSolvingScore").value(4))
                .andExpect(jsonPath("$.data.growthPotentialScore").value(3))
                .andExpect(jsonPath("$.data.summary").value("좋은 요약"));

        verify(interviewService).getInterviewAnalysis(sessionId);
    }
}
