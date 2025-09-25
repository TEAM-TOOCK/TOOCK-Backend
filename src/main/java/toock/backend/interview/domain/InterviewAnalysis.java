package toock.backend.interview.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false)
    private InterviewSession interviewSession;

    @Column
    private Integer score;

    @Column
    private Integer technicalExpertiseScore;

    @Column
    private Integer collaborationCommunicationScore;

    @Column
    private Integer problemSolvingScore;

    @Column
    private Integer growthPotentialScore;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String summary;

    @Builder
    public InterviewAnalysis(InterviewSession interviewSession,
                            Integer score,
                            Integer technicalExpertiseScore,
                            Integer collaborationCommunicationScore,
                            Integer problemSolvingScore,
                            Integer growthPotentialScore,
                            String summary) {
        this.interviewSession = interviewSession;
        this.score = score;
        this.technicalExpertiseScore = technicalExpertiseScore;
        this.collaborationCommunicationScore = collaborationCommunicationScore;
        this.problemSolvingScore = problemSolvingScore;
        this.growthPotentialScore = growthPotentialScore;
        this.summary = summary;
    }
}


