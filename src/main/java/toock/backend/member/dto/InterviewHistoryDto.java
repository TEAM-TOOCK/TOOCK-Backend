package toock.backend.member.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.format.DateTimeFormatter;
import toock.backend.interview.domain.InterviewAnalysis;
import toock.backend.interview.domain.InterviewFieldCategory;
import toock.backend.interview.domain.InterviewSession;
import toock.backend.member.domain.Field;

@Getter
@Builder
public class InterviewHistoryDto {
    private Long interviewSessionId;
    private String companyName;
    private InterviewFieldCategory interviewFieldCategory;
    private Field field;
    private String date;
    private Integer score;
    private Integer maxScore;
    private Long questionCount;

    public static InterviewHistoryDto of(InterviewSession session, InterviewAnalysis analysis, Long questionCount) {
        return InterviewHistoryDto.builder()
                .interviewSessionId(session.getId())
                .companyName(session.getCompany().getName())
                .interviewFieldCategory(session.getFieldCategory())
                .field(session.getField())
                .date(session.getStartedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .score(analysis != null ? analysis.getScore() : 0)
                .maxScore(5)
                .questionCount(questionCount != null ? questionCount : 0L)
                .build();
    }
}
