package toock.backend.interview.service;

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
import toock.backend.interview.dto.InterviewResultResponseDto;
import toock.backend.interview.repository.InterviewAnalysisRepository;
import toock.backend.interview.repository.InterviewQARepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterviewResultServiceTest {

    @InjectMocks
    private InterviewResultService interviewResultService;

    @Mock
    private InterviewAnalysisRepository interviewAnalysisRepository;

    @Mock
    private InterviewQARepository interviewQARepository;

    private Long interviewSessionId = 1L;
    private InterviewSession mockInterviewSession;
    private InterviewAnalysis mockInterviewAnalysis;
    private InterviewQA mockInterviewQA1;
    private InterviewQA mockInterviewQA2;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("면접 결과 상세 조회 성공")
    void getInterviewResultDetails_Success() {
        // Given
        InterviewSession mockInterviewSession = mock(InterviewSession.class);

        InterviewAnalysis mockInterviewAnalysis = mock(InterviewAnalysis.class);
        when(mockInterviewAnalysis.getId()).thenReturn(10L);
        when(mockInterviewAnalysis.getSummary()).thenReturn("Summary Test");
        when(mockInterviewAnalysis.getTechnicalExpertiseScore()).thenReturn(5);
        when(mockInterviewAnalysis.getCollaborationCommunicationScore()).thenReturn(4);
        when(mockInterviewAnalysis.getProblemSolvingScore()).thenReturn(3);
        when(mockInterviewAnalysis.getGrowthPotentialScore()).thenReturn(2);

        InterviewQA mockInterviewQA1 = mock(InterviewQA.class);
        when(mockInterviewQA1.getId()).thenReturn(100L);
        when(mockInterviewQA1.getQuestionOrder()).thenReturn(1);
        when(mockInterviewQA1.getFollowUpOrder()).thenReturn(0);
        when(mockInterviewQA1.getQuestionText()).thenReturn("Question 1");
        when(mockInterviewQA1.getAnswerText()).thenReturn("Answer 1");
        when(mockInterviewQA1.getS3Url()).thenReturn("s3://url1");

        InterviewQA mockInterviewQA2 = mock(InterviewQA.class);
        when(mockInterviewQA2.getId()).thenReturn(101L);
        when(mockInterviewQA2.getQuestionOrder()).thenReturn(2);
        when(mockInterviewQA2.getFollowUpOrder()).thenReturn(0);
        when(mockInterviewQA2.getQuestionText()).thenReturn("Question 2");
        when(mockInterviewQA2.getAnswerText()).thenReturn("Answer 2");
        when(mockInterviewQA2.getS3Url()).thenReturn("s3://url2");
        
        when(interviewAnalysisRepository.findByInterviewSessionId(interviewSessionId))
                .thenReturn(Optional.of(mockInterviewAnalysis));
        when(interviewQARepository.findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(interviewSessionId))
                .thenReturn(Arrays.asList(mockInterviewQA1, mockInterviewQA2));

        // When
        InterviewResultResponseDto result = interviewResultService.getInterviewResultDetails(interviewSessionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getInterviewAnalysisId()).isEqualTo(10L);
        assertThat(result.getInterviewSessionId()).isEqualTo(interviewSessionId);
        assertThat(result.getTotalScore()).isEqualTo(5 + 4 + 3 + 2);
        assertThat(result.getTechnicalExpertiseScore()).isEqualTo(5);
        assertThat(result.getSoftSkillsScore()).isEqualTo(4);
        assertThat(result.getProblemSolvingScore()).isEqualTo(3);
        assertThat(result.getGrowthPotentialScore()).isEqualTo(2);
        assertThat(result.getAiFeedback()).isEqualTo("Summary Test");
        assertThat(result.getStrengths()).isNull();
        assertThat(result.getImprovements()).isNull();
        assertThat(result.getQaRecords()).hasSize(2);

        assertThat(result.getQaRecords().get(0).getInterviewQAId()).isEqualTo(100L);
        assertThat(result.getQaRecords().get(0).getQuestionText()).isEqualTo("Question 1");
        assertThat(result.getQaRecords().get(0).getAnswerText()).isEqualTo("Answer 1");
        assertThat(result.getQaRecords().get(0).getS3Url()).isEqualTo("s3://url1");

        assertThat(result.getQaRecords().get(1).getInterviewQAId()).isEqualTo(101L);
        assertThat(result.getQaRecords().get(1).getQuestionText()).isEqualTo("Question 2");
        assertThat(result.getQaRecords().get(1).getAnswerText()).isEqualTo("Answer 2");
        assertThat(result.getQaRecords().get(1).getS3Url()).isEqualTo("s3://url2");

        verify(interviewAnalysisRepository, times(1)).findByInterviewSessionId(interviewSessionId);
        verify(interviewQARepository, times(1)).findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(interviewSessionId);
    }

    @Test
    @DisplayName("면접 분석을 찾을 수 없는 경우 예외 발생")
    void getInterviewResultDetails_AnalysisNotFound() {
        // Given
        InterviewAnalysis mockInterviewAnalysis = mock(InterviewAnalysis.class);

        when(interviewAnalysisRepository.findByInterviewSessionId(interviewSessionId))
                .thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                interviewResultService.getInterviewResultDetails(interviewSessionId)
        );

        assertThat(exception.getMessage()).isEqualTo("면접 분석을 찾을 수 없습니다. ID: " + interviewSessionId);
        verify(interviewAnalysisRepository, times(1)).findByInterviewSessionId(interviewSessionId);
        verify(interviewQARepository, never()).findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(anyLong());
    }

    @Test
    @DisplayName("QA 기록이 없는 경우 예외 발생")
    void getInterviewResultDetails_NoQARecords() {
        // Given
        InterviewAnalysis mockInterviewAnalysis = mock(InterviewAnalysis.class);

        when(interviewAnalysisRepository.findByInterviewSessionId(interviewSessionId))
                .thenReturn(Optional.of(mockInterviewAnalysis));
        when(interviewQARepository.findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(interviewSessionId))
                .thenReturn(List.of());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                interviewResultService.getInterviewResultDetails(interviewSessionId)
        );

        assertThat(exception.getMessage()).isEqualTo("면접 질문-답변 기록을 찾을 수 없습니다. ID: " + interviewSessionId);
        verify(interviewAnalysisRepository, times(1)).findByInterviewSessionId(interviewSessionId);
        verify(interviewQARepository, times(1)).findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(interviewSessionId);
    }
}
