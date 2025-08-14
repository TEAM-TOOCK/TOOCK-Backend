package toock.backend.interview.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "interviewqa",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_interview_question_order",
                             columnNames = {"interview_id", "question_order"})
       })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewQA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false)
    private InterviewSession interviewSession;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Lob
    @Column(nullable = false)
    private String questionText;

    @Lob
    @Column
    private String answerText;

    @Column
    private Integer responseTimeSeconds;

    @Column(length = 500)
    private String s3Url;

    @Builder
    public InterviewQA(InterviewSession interviewSession,
                       Integer questionOrder,
                       String questionText,
                       String answerText,
                       Integer responseTimeSeconds,
                       String s3Url) {
        this.interviewSession = interviewSession;
        this.questionOrder = questionOrder;
        this.questionText = questionText;
        this.answerText = answerText;
        this.responseTimeSeconds = responseTimeSeconds;
        this.s3Url = s3Url;
    }
}


