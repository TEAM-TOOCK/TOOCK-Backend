package toock.backend.interview.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "interview_qa",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_interview_question_order",
                        columnNames = {"interview_session_id", "question_order", "follow_up_order"})
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewQA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_session_id", nullable = false)
    private InterviewSession interviewSession;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder; // 주요 질문의 순서 (1~5)

    @Column(name = "follow_up_order", nullable = false)
    private Integer followUpOrder; // 꼬리 질문의 순서 (주요 질문은 0)

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String answerText;

//    @Column
//    private Integer responseTimeSeconds;

    @Column(length = 500)
    private String s3Url;

    @Builder
    public InterviewQA(InterviewSession interviewSession, Integer questionOrder, Integer followUpOrder, String questionText) {
        this.interviewSession = interviewSession;
        this.questionOrder = questionOrder;
        this.followUpOrder = followUpOrder;
        this.questionText = questionText;
    }

    public void updateAnswer(String answerText, String s3Url) {
        this.answerText = answerText;
        this.s3Url = s3Url;
    }
}