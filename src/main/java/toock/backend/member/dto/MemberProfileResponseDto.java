package toock.backend.member.dto;

import lombok.Builder;
import lombok.Getter;
import toock.backend.interview.domain.InterviewFieldCategory;
import toock.backend.member.domain.Field;
import toock.backend.member.domain.Member;

@Getter
@Builder
public class MemberProfileResponseDto {
    private Field field;
    private InterviewFieldCategory interviewFieldCategory;

    public static MemberProfileResponseDto from(Member member) {
        return MemberProfileResponseDto.builder()
                .field(member.getField())
                .interviewFieldCategory(member.getInterviewFieldCategory())
                .build();
    }
}