package toock.backend.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import toock.backend.interview.domain.InterviewAnalysis;
import toock.backend.interview.domain.InterviewQA;
import toock.backend.interview.domain.InterviewSession;
import toock.backend.interview.dto.InterviewAnalysisResponseDto;
import toock.backend.interview.dto.InterviewEvaluationResult;
import toock.backend.interview.repository.InterviewAnalysisRepository;
import toock.backend.interview.repository.InterviewQARepository;
import toock.backend.interview.repository.InterviewSessionRepository;
import toock.backend.user.repository.MemberRepository;
import toock.backend.company.repository.CompanyRepository;
import toock.backend.company.repository.CompanyReviewRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterviewServiceTest {

    @InjectMocks
    private InterviewService interviewService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CompanyReviewRepository companyReviewRepository;
    @Mock
    private InterviewSessionRepository interviewSessionRepository;
    @Mock
    private InterviewQARepository interviewQARepository;
    @Mock
    private InterviewAnalysisRepository interviewAnalysisRepository;
    @Mock
    private PromptService promptService;
    @Mock
    private GeminiService geminiService;
    @Mock
    private ObjectMapper objectMapper;

    private InterviewSession testSession;
    private InterviewQA testQA1;
    private InterviewQA testQA2;

    @BeforeEach
    void setUp() {
        testSession = mock(InterviewSession.class);
        lenient().when(testSession.getId()).thenReturn(1L);

        testQA1 = mock(InterviewQA.class);
        lenient().when(testQA1.getQuestionText()).thenReturn("질문1");
        lenient().when(testQA1.getAnswerText()).thenReturn("답변1");

        testQA2 = mock(InterviewQA.class);
        lenient().when(testQA2.getQuestionText()).thenReturn("질문2");
        lenient().when(testQA2.getAnswerText()).thenReturn("답변2");
    }

    @Test
    @DisplayName("면접 평가 성공")
    void evaluateInterview_Success() throws Exception {
        Long sessionId = 1L;
        String geminiResponseJson = "{\"totalScore\": 4, \"technicalExpertiseScore\": 5, \"collaborationCommunicationScore\": 4, \"problemSolvingScore\": 4, \"growthPotentialScore\": 3, \"summary\": \"좋은 요약\"}";
        InterviewEvaluationResult mockResult = new InterviewEvaluationResult();
        mockResult.setTotalScore(4);
        mockResult.setTechnicalExpertiseScore(5);
        mockResult.setCollaborationCommunicationScore(4);
        mockResult.setProblemSolvingScore(4);
        mockResult.setGrowthPotentialScore(3);
        mockResult.setSummary("좋은 요약");

        lenient().when(interviewSessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        lenient().when(interviewQARepository.findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(sessionId))
                .thenReturn(Arrays.asList(testQA1, testQA2));
        lenient().when(interviewAnalysisRepository.findByInterviewSessionId(sessionId)).thenReturn(Optional.empty());
        lenient().when(promptService.createInterviewEvaluationPrompt(anyList())).thenReturn("평가 프롬프트");
        lenient().when(geminiService.generateQuestion(anyString())).thenReturn(geminiResponseJson);
        lenient().when(objectMapper.readValue(anyString(), eq(InterviewEvaluationResult.class)))
                .thenReturn(mockResult);

        InterviewAnalysis savedAnalysis = mock(InterviewAnalysis.class);
        lenient().when(savedAnalysis.getId()).thenReturn(1L);
        lenient().when(savedAnalysis.getInterviewSession()).thenReturn(testSession);
        lenient().when(savedAnalysis.getScore()).thenReturn(mockResult.getTotalScore());
        lenient().when(savedAnalysis.getTechnicalExpertiseScore()).thenReturn(mockResult.getTechnicalExpertiseScore());
        lenient().when(savedAnalysis.getCollaborationCommunicationScore()).thenReturn(mockResult.getCollaborationCommunicationScore());
        lenient().when(savedAnalysis.getProblemSolvingScore()).thenReturn(mockResult.getProblemSolvingScore());
        lenient().when(savedAnalysis.getGrowthPotentialScore()).thenReturn(mockResult.getGrowthPotentialScore());
        lenient().when(savedAnalysis.getSummary()).thenReturn(mockResult.getSummary());

        lenient().when(interviewAnalysisRepository.save(any(InterviewAnalysis.class))).thenReturn(savedAnalysis);

        InterviewAnalysisResponseDto response = interviewService.evaluateInterview(sessionId);

        assertThat(response.getInterviewSessionId()).isEqualTo(sessionId);
        assertThat(response.getScore()).isEqualTo(4);
        assertThat(response.getTechnicalExpertiseScore()).isEqualTo(5);
        assertThat(response.getCollaborationCommunicationScore()).isEqualTo(4);
        assertThat(response.getProblemSolvingScore()).isEqualTo(4);
        assertThat(response.getGrowthPotentialScore()).isEqualTo(3);
        assertThat(response.getSummary()).isEqualTo("좋은 요약");

        verify(interviewSessionRepository).findById(sessionId);
        verify(interviewQARepository).findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(sessionId);
        verify(interviewAnalysisRepository).findByInterviewSessionId(sessionId);
        verify(promptService).createInterviewEvaluationPrompt(anyList());
        verify(geminiService).generateQuestion(anyString());
        verify(objectMapper).readValue(anyString(), eq(InterviewEvaluationResult.class));
        verify(interviewAnalysisRepository).save(any(InterviewAnalysis.class));
    }

    @Test
    @DisplayName("면접 세션이 없는 경우 예외 발생")
    void evaluateInterview_SessionNotFound() throws Exception {
        Long sessionId = 1L;
        lenient().when(interviewSessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> interviewService.evaluateInterview(sessionId));

        verify(interviewSessionRepository).findById(sessionId);
        verify(interviewQARepository, never()).findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(anyLong());
        verify(interviewAnalysisRepository, never()).findByInterviewSessionId(anyLong());
        verify(promptService, never()).createInterviewEvaluationPrompt(anyList());
        verify(geminiService, never()).generateQuestion(anyString());
        verify(objectMapper, never()).readValue(anyString(), eq(InterviewEvaluationResult.class));
        verify(interviewAnalysisRepository, never()).save(any(InterviewAnalysis.class));
    }

    @Test
    @DisplayName("면접 질문-답변 데이터가 없는 경우 예외 발생")
    void evaluateInterview_NoQAs() throws Exception {
        Long sessionId = 1L;
        lenient().when(interviewSessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        lenient().when(interviewQARepository.findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(sessionId)).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> interviewService.evaluateInterview(sessionId));

        verify(interviewSessionRepository).findById(sessionId);
        verify(interviewQARepository).findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(sessionId);
        verify(interviewAnalysisRepository, never()).findByInterviewSessionId(anyLong());
        verify(promptService, never()).createInterviewEvaluationPrompt(anyList());
        verify(geminiService, never()).generateQuestion(anyString());
        verify(objectMapper, never()).readValue(anyString(), eq(InterviewEvaluationResult.class));
        verify(interviewAnalysisRepository, never()).save(any(InterviewAnalysis.class));
    }

    @Test
    @DisplayName("Gemini 응답 파싱 실패 시 예외 발생")
    void evaluateInterview_GeminiResponseParsingFailure() throws Exception {
        Long sessionId = 1L;
        String malformedJson = "잘못된 JSON";

        lenient().when(interviewSessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        lenient().when(interviewQARepository.findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(sessionId))
                .thenReturn(Arrays.asList(testQA1, testQA2));
        lenient().when(interviewAnalysisRepository.findByInterviewSessionId(sessionId)).thenReturn(Optional.empty());
        lenient().when(promptService.createInterviewEvaluationPrompt(anyList())).thenReturn("평가 프롬프트");
        lenient().when(geminiService.generateQuestion(anyString())).thenReturn(malformedJson);
        lenient().when(objectMapper.readValue(anyString(), eq(InterviewEvaluationResult.class)))
                .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "파싱 오류"));

        assertThrows(IllegalStateException.class, () -> interviewService.evaluateInterview(sessionId));

        verify(interviewSessionRepository).findById(sessionId);
        verify(interviewQARepository).findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(sessionId);
        verify(interviewAnalysisRepository).findByInterviewSessionId(sessionId);
        verify(promptService).createInterviewEvaluationPrompt(anyList());
        verify(geminiService).generateQuestion(anyString());
        verify(objectMapper).readValue(anyString(), eq(InterviewEvaluationResult.class));
        verify(interviewAnalysisRepository, never()).save(any(InterviewAnalysis.class));
    }

    @Test
    @DisplayName("이미 평가된 면접 세션인 경우 기존 결과 반환")
    void evaluateInterview_AlreadyAnalyzed() throws Exception {
        Long sessionId = 1L;
        InterviewAnalysis existingAnalysis = mock(InterviewAnalysis.class);
        lenient().when(existingAnalysis.getId()).thenReturn(100L);
        lenient().when(existingAnalysis.getInterviewSession()).thenReturn(testSession);
        lenient().when(existingAnalysis.getScore()).thenReturn(3);
        lenient().when(existingAnalysis.getTechnicalExpertiseScore()).thenReturn(3);
        lenient().when(existingAnalysis.getCollaborationCommunicationScore()).thenReturn(3);
        lenient().when(existingAnalysis.getProblemSolvingScore()).thenReturn(3);
        lenient().when(existingAnalysis.getGrowthPotentialScore()).thenReturn(3);
        lenient().when(existingAnalysis.getSummary()).thenReturn("기존 요약");

        lenient().when(interviewSessionRepository.findById(sessionId)).thenReturn(Optional.of(testSession));
        lenient().when(interviewQARepository.findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(sessionId))
                .thenReturn(Arrays.asList(testQA1, testQA2));
        lenient().when(interviewAnalysisRepository.findByInterviewSessionId(sessionId)).thenReturn(Optional.of(existingAnalysis));

        InterviewAnalysisResponseDto response = interviewService.evaluateInterview(sessionId);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getInterviewSessionId()).isEqualTo(sessionId);
        assertThat(response.getScore()).isEqualTo(3);
        assertThat(response.getTechnicalExpertiseScore()).isEqualTo(3);
        assertThat(response.getCollaborationCommunicationScore()).isEqualTo(3);
        assertThat(response.getProblemSolvingScore()).isEqualTo(3);
        assertThat(response.getGrowthPotentialScore()).isEqualTo(3);
        assertThat(response.getSummary()).isEqualTo("기존 요약");

        verify(interviewSessionRepository).findById(sessionId);
        verify(interviewQARepository).findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(sessionId);
        verify(interviewAnalysisRepository).findByInterviewSessionId(sessionId);
        verify(promptService, never()).createInterviewEvaluationPrompt(anyList());
        verify(geminiService, never()).generateQuestion(anyString());
        verify(objectMapper, never()).readValue(anyString(), eq(InterviewEvaluationResult.class));
        verify(interviewAnalysisRepository, never()).save(any(InterviewAnalysis.class));
    }
}
