package toock.backend.interview.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class QAItemDto {
    private Long interviewQAId;
    private Integer questionOrder;
    private Integer followUpOrder;
    private String questionText;
    private String answerText;
    private String s3Url;
    private String evaluation;
    private Integer score;
    private String fieldCategory;
}
