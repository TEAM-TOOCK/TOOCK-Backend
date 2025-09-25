package toock.backend.interview.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterviewEvaluationResult {
    private Integer totalScore;
    private Integer technicalExpertiseScore;
    private Integer collaborationCommunicationScore;
    private Integer problemSolvingScore;
    private Integer growthPotentialScore;
    private String summary;
}
