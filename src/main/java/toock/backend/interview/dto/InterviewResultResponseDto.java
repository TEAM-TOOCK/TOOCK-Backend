package toock.backend.interview.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class InterviewResultResponseDto {
    private Long interviewAnalysisId;
    private Long interviewSessionId;
    private Integer totalScore;
    private Integer technicalExpertiseScore;
    private Integer softSkillsScore;
    private Integer problemSolvingScore;
    private Integer growthPotentialScore;
    private String aiFeedback;
    private List<QAItemDto> qaRecords;
    private String strengths;
    private String improvements;
}
