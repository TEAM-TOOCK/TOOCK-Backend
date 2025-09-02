package toock.backend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toock.backend.interview.domain.InterviewSession;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
}