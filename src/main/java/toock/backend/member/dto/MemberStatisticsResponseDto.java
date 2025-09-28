package toock.backend.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberStatisticsResponseDto {
    private long totalInterviews;
    private double averageScore;
    private Integer bestScore;
    private long interviewsThisWeek;
}