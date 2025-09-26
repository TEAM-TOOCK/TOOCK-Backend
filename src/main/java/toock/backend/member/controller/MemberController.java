package toock.backend.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import toock.backend.global.dto.CommonResponseDto;
import toock.backend.member.dto.MemberNicknameResponseDto;
import toock.backend.member.dto.MemberStatisticsResponseDto;
import toock.backend.member.service.MemberService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/nickname")
    public ResponseEntity<CommonResponseDto<MemberNicknameResponseDto>> getMemberNickname(
            @AuthenticationPrincipal Long memberId) {
        MemberNicknameResponseDto response = memberService.getMemberNickname(memberId);
        return ResponseEntity.ok(CommonResponseDto.success(response));
    }

    @GetMapping("/statistics")
    public ResponseEntity<CommonResponseDto<MemberStatisticsResponseDto>> getUserStatistics(
            @AuthenticationPrincipal Long memberId) {
        MemberStatisticsResponseDto statistics = memberService.getUserStatistics(memberId);
        return ResponseEntity.ok(CommonResponseDto.success(statistics));
    }
}


