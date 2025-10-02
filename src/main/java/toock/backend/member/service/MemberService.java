package toock.backend.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toock.backend.global.error.ErrorCode;
import toock.backend.interview.domain.InterviewAnalysis;
import toock.backend.interview.domain.InterviewSession;
import toock.backend.interview.repository.InterviewAnalysisRepository;
import toock.backend.interview.repository.InterviewQARepository;
import toock.backend.interview.repository.InterviewSessionRepository;
import toock.backend.member.domain.Member;
import toock.backend.member.dto.*;
import toock.backend.member.error.MemberException;
import toock.backend.member.repository.MemberRepository;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewAnalysisRepository interviewAnalysisRepository;
    private final InterviewQARepository interviewQARepository;


    @Transactional(readOnly = true)
    public MemberNicknameResponseDto getMemberNickname(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberNicknameResponseDto.builder()
                .nickname(member.getName()) // Member 엔티티의 name 필드를 반환
                .build();
    }

    @Transactional(readOnly = true)
    public MemberStatisticsResponseDto getUserStatistics(Long memberId) {
        // 1. 사용자의 모든 면접 세션 ID 조회
        List<Long> sessionIds = interviewSessionRepository.findByMemberId(memberId)
                .stream()
                .map(InterviewSession::getId)
                .collect(Collectors.toList());

        // 2. 완료된 면접에 대한 모든 분석 결과 조회
        List<InterviewAnalysis> analyses = interviewAnalysisRepository.findByInterviewSessionIdIn(sessionIds);


        // 3. 통계 계산
        long totalInterviews = analyses.size();
        double averageScore = analyses.stream()
                .mapToInt(InterviewAnalysis::getScore)
                .average()
                .orElse(0.0);
        Integer bestScore = analyses.stream()
                .mapToInt(InterviewAnalysis::getScore)
                .max()
                .orElse(0);

        // 4. 이번 주에 '분석까지 완료된' 면접 횟수 계산 (월요일 시작 ~ 일요일 끝)
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0);
        OffsetDateTime endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).withHour(23).withMinute(59).withSecond(59);

        long interviewsThisWeek = interviewAnalysisRepository.countAnalyzedInterviewsByMemberAndDateRange(memberId, startOfWeek, endOfWeek);

        return MemberStatisticsResponseDto.builder()
                .totalInterviews(totalInterviews)
                .averageScore(averageScore)
                .bestScore(bestScore)
                .interviewsThisWeek(interviewsThisWeek)
                .build();
    }

    @Transactional
    public void updateProfile(Long memberId, MemberProfileUpdateRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateProfile(requestDto.getField(), requestDto.getInterviewFieldCategory());
    }

    @Transactional(readOnly = true)
    public MemberProfileResponseDto getMemberProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND) );
        return MemberProfileResponseDto.from(member);
    }

    @Transactional(readOnly = true)
    public List<InterviewHistoryDto> getInterviewHistories(Long memberId) {
        List<InterviewSession> sessions = interviewSessionRepository.findByMemberId(memberId)
                .stream()
                .sorted(Comparator.comparing(InterviewSession::getStartedAt).reversed())
                .toList();

        return sessions.stream().map(session -> {
            Optional<InterviewAnalysis> analysisOpt = interviewAnalysisRepository.findByInterviewSessionId(session.getId());
            Long questionCount = interviewQARepository.countByInterviewSessionId(session.getId());

            return InterviewHistoryDto.of(session, analysisOpt.orElse(null), questionCount);
        }).collect(Collectors.toList());
    }
}