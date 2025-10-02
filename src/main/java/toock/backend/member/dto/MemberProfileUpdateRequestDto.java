package toock.backend.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import toock.backend.interview.domain.InterviewFieldCategory;
import toock.backend.member.domain.Field;

@Getter
@NoArgsConstructor
public class MemberProfileUpdateRequestDto {
    private Field field;
    private InterviewFieldCategory interviewFieldCategory;
}