package toock.backend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import toock.backend.interview.domain.InterviewAnalysis;

import java.util.Optional;

@Repository
public interface InterviewAnalysisRepository extends JpaRepository<InterviewAnalysis, Long> {
    Optional<InterviewAnalysis> findByInterviewSessionId(Long interviewSessionId);
}
