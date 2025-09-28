package toock.backend.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberNicknameResponseDto {
    private String nickname;
}