package toock.backend.interview.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class InterviewAnalysisResponseDto {
    private Long id;
    private Long interviewSessionId;
    private Integer score;
    private Integer technicalExpertiseScore;
    private Integer collaborationCommunicationScore;
    private Integer problemSolvingScore;
    private Integer growthPotentialScore;
    private String summary;
    private String strengths;
    private String improvements;
}
