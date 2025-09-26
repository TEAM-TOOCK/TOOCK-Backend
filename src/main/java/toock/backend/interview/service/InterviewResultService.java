package toock.backend.interview.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toock.backend.interview.domain.InterviewAnalysis;
import toock.backend.interview.domain.InterviewQA;
import toock.backend.interview.dto.InterviewResultResponseDto;
import toock.backend.interview.dto.QAItemDto;
import toock.backend.interview.repository.InterviewAnalysisRepository;
import toock.backend.interview.repository.InterviewQARepository;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewResultService {

    private final InterviewAnalysisRepository interviewAnalysisRepository;
    private final InterviewQARepository interviewQARepository;

    @Transactional(readOnly = true)
    public InterviewResultResponseDto getInterviewResultDetails(Long interviewSessionId) {
        InterviewAnalysis analysis = interviewAnalysisRepository.findByInterviewSessionId(interviewSessionId)
                .orElseThrow(() -> new IllegalArgumentException("면접 분석을 찾을 수 없습니다. ID: " + interviewSessionId));

        List<InterviewQA> qas = interviewQARepository.findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(interviewSessionId);

        if (qas.isEmpty()) {
            throw new IllegalArgumentException("면접 질문-답변 기록을 찾을 수 없습니다. ID: " + interviewSessionId);
        }

        List<QAItemDto> qaRecords = qas.stream()
                .map(qa -> QAItemDto.builder()
                        .interviewQAId(qa.getId())
                        .questionOrder(qa.getQuestionOrder())
                        .followUpOrder(qa.getFollowUpOrder())
                        .questionText(qa.getQuestionText())
                        .answerText(qa.getAnswerText())
                        .s3Url(qa.getS3Url())
                        .evaluation(null)
                        .score(null)
                        .fieldCategory(null)
                        .build())
                .collect(Collectors.toList());

        String summary = analysis.getSummary() != null ? analysis.getSummary() : "";

        // totalScore는 4항목 점수의 합계로 산정하자
        Integer technicalExpertiseScore = analysis.getTechnicalExpertiseScore();
        Integer softSkillsScore = analysis.getCollaborationCommunicationScore();
        Integer problemSolvingScore = analysis.getProblemSolvingScore();
        Integer growthPotentialScore = analysis.getGrowthPotentialScore();

        Integer totalScore = (technicalExpertiseScore != null ? technicalExpertiseScore : 0) +
                             (softSkillsScore != null ? softSkillsScore : 0) +
                             (problemSolvingScore != null ? problemSolvingScore : 0) +
                             (growthPotentialScore != null ? growthPotentialScore : 0);

        // TODO: 프롬프트로 분석 레코드에 생성하자
        String strengths = null;
        String improvements = null;

        return InterviewResultResponseDto.builder()
                .interviewAnalysisId(analysis.getId())
                .interviewSessionId(interviewSessionId)
                .totalScore(totalScore)
                .technicalExpertiseScore(technicalExpertiseScore)
                .softSkillsScore(softSkillsScore)
                .problemSolvingScore(problemSolvingScore)
                .growthPotentialScore(growthPotentialScore)
                .aiFeedback(summary)
                .qaRecords(qaRecords)
                .strengths(strengths)
                .improvements(improvements)
                .build();
    }
}
