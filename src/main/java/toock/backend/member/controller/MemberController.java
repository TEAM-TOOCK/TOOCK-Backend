package toock.backend.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import toock.backend.global.dto.CommonResponseDto;
import toock.backend.member.dto.*;
import toock.backend.member.service.MemberService;

import java.util.List;

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

    @PutMapping("/profile")
    public ResponseEntity<CommonResponseDto<Void>> updateUserProfile(
            @AuthenticationPrincipal Long memberId,
            @RequestBody MemberProfileUpdateRequestDto requestDto) {
        memberService.updateProfile(memberId, requestDto);
        return ResponseEntity.ok(CommonResponseDto.success(null));
    }

    @GetMapping("/profile")
    public ResponseEntity<CommonResponseDto<MemberProfileResponseDto>> getMemberProfile(
            @AuthenticationPrincipal Long memberId) {
        MemberProfileResponseDto response = memberService.getMemberProfile(memberId);
        return ResponseEntity.ok(CommonResponseDto.success(response));
    }

    @GetMapping("/interviews")
    public ResponseEntity<CommonResponseDto<List<InterviewHistoryDto>>> getInterviewHistories(
            @AuthenticationPrincipal Long memberId) {
        List<InterviewHistoryDto> histories = memberService.getInterviewHistories(memberId);
        return ResponseEntity.ok(CommonResponseDto.success(histories));
    }
}


