package toock.backend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import toock.backend.interview.domain.InterviewAnalysis;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewAnalysisRepository extends JpaRepository<InterviewAnalysis, Long> {
    Optional<InterviewAnalysis> findByInterviewSessionId(Long interviewSessionId);

    List<InterviewAnalysis> findByInterviewSessionIdIn(List<Long> interviewSessionIds);

    /**
     * 특정 사용자의 특정 기간 내에 시작된 면접 중 분석이 완료된 면접의 수를 계산합니다.
     * @param memberId 사용자의 ID
     * @param startOfWeek 주의 시작 시간
     * @param endOfWeek 주의 종료 시간
     * @return 분석 완료된 면접 수
     */
    @Query("SELECT COUNT(ia) FROM InterviewAnalysis ia " +
           "JOIN ia.interviewSession s " +
           "WHERE s.member.id = :memberId " +
           "AND s.startedAt BETWEEN :startOfWeek AND :endOfWeek")
    long countAnalyzedInterviewsByMemberAndDateRange(
            @Param("memberId") Long memberId,
            @Param("startOfWeek") OffsetDateTime startOfWeek,
            @Param("endOfWeek") OffsetDateTime endOfWeek);
}
