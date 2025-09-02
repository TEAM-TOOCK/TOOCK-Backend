package toock.backend.interview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import toock.backend.interview.domain.InterviewQA;

import java.util.List;
import java.util.Optional;

public interface InterviewQARepository extends JpaRepository<InterviewQA, Long> {

    // 세션의 모든 질문-답변을 순서대로 조회
    List<InterviewQA> findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(Long interviewSessionId);

    // 세션의 가장 마지막 질문(꼬리질문 포함) 조회
    Optional<InterviewQA> findTopByInterviewSession_IdOrderByQuestionOrderDescFollowUpOrderDesc(Long interviewSessionId);

    // 세션과 주요 질문 순서로 다음 '주요 질문'(`followUpOrder = 0`)을 조회
    Optional<InterviewQA> findByInterviewSession_IdAndQuestionOrderAndFollowUpOrder(Long interviewSessionId, Integer questionOrder, Integer followUpOrder);
}