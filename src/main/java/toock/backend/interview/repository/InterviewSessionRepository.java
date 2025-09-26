package toock.backend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toock.backend.interview.domain.InterviewSession;

import java.time.OffsetDateTime;
import java.util.List;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByMemberId(Long memberId);

    long countByMemberIdAndStartedAtBetween(Long memberId, OffsetDateTime start, OffsetDateTime end);
}