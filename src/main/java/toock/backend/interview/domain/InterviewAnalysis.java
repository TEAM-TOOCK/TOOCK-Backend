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
    private Integer technicalScore;

    @Column
    private Integer attitudeScore;

    @Lob
    @Column
    private String summary;

    @Builder
    public InterviewAnalysis(InterviewSession interviewSession,
                            Integer score,
                            Integer technicalScore,
                            Integer attitudeScore,
                            String summary) {
        this.interviewSession = interviewSession;
        this.score = score;
        this.technicalScore = technicalScore;
        this.attitudeScore = attitudeScore;
        this.summary = summary;
    }
}


