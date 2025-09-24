package toock.backend.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toock.backend.company.domain.Company;
import toock.backend.company.domain.CompanyReview;
import toock.backend.company.repository.CompanyRepository;
import toock.backend.company.repository.CompanyReviewRepository;
import toock.backend.interview.domain.InterviewQA;
import toock.backend.interview.domain.InterviewSession;
import toock.backend.interview.dto.InterviewDto;
import toock.backend.interview.repository.InterviewQARepository;
import toock.backend.interview.repository.InterviewSessionRepository;
import toock.backend.user.domain.Member;
import toock.backend.user.repository.MemberRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewService {

    private final MemberRepository memberRepository;
    private final CompanyRepository companyRepository;
    private final CompanyReviewRepository companyReviewRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewQARepository interviewQARepository;
    private final PromptService promptService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    private static final int MAX_MAIN_QUESTIONS = 3;
    private static final int MAX_FOLLOW_UP_QUESTIONS = 1;

    @Transactional
    public InterviewDto.StartResponse startInterview(InterviewDto.StartRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + request.getMemberId()));
        Company company = companyRepository.findByName(request.getCompanyName())
                .orElseThrow(() -> new IllegalArgumentException("회사를 찾을 수 없습니다. 이름: " + request.getCompanyName()));

        InterviewSession session = InterviewSession.builder()
                .member(member)
                .company(company)
                .field(request.getField())
                .status("IN_PROGRESS")
                .startedAt(OffsetDateTime.now())
                .build();
        interviewSessionRepository.save(session);

        List<CompanyReview> reviews = companyReviewRepository.findByCompany_Name(company.getName());
        String contextData = formatReviewsForPrompt(reviews);
        String mainQuestionsPrompt = promptService.createMainQuestionsPrompt(contextData, request.getField().name());
        String rawResponse = geminiService.generateQuestion(mainQuestionsPrompt);

        String cleanJson = sanitizeJsonResponse(rawResponse);
        List<String> mainQuestions;
        try {
            mainQuestions = objectMapper.readValue(cleanJson, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Gemini 응답 JSON 파싱 실패. 원본: {}, 정리 후: {}", rawResponse, cleanJson, e);
            throw new IllegalStateException("Gemini로부터 받은 질문 형식이 올바르지 않습니다.");
        }

        if (mainQuestions.isEmpty()) {
            throw new IllegalStateException("Gemini로부터 유효한 질문을 생성하지 못했습니다.");
        }

        for (int i = 0; i < mainQuestions.size() && i < MAX_MAIN_QUESTIONS; i++) {
            InterviewQA mainQA = InterviewQA.builder()
                    .interviewSession(session)
                    .questionOrder(i + 1)
                    .followUpOrder(0)
                    .questionText(mainQuestions.get(i).trim())
                    .build();
            interviewQARepository.save(mainQA);
        }

        return new InterviewDto.StartResponse(session.getId(), mainQuestions.get(0).trim());
    }

    @Transactional
    public InterviewDto.NextResponse nextQuestion(InterviewDto.NextRequest request) {
        InterviewSession session = interviewSessionRepository.findById(request.getInterviewSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다. ID: " + request.getInterviewSessionId()));

        List<InterviewQA> allQAs = interviewQARepository.findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(session.getId());

        InterviewQA currentQA = allQAs.stream()
                .filter(qa -> qa.getAnswerText() == null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("답변할 질문을 찾을 수 없습니다. 모든 질문에 답변이 완료되었습니다."));

        currentQA.updateAnswer(request.getAnswerText(), request.getS3Url());

        List<String> conversationHistory = buildConversationHistory(allQAs);

        if (currentQA.getFollowUpOrder() < MAX_FOLLOW_UP_QUESTIONS && shouldAskFollowUp(conversationHistory)) {
            String followUpPrompt = promptService.createFollowUpPrompt(conversationHistory);
            String followUpQuestionText = geminiService.generateQuestion(followUpPrompt);

            InterviewQA followUpQA = InterviewQA.builder()
                    .interviewSession(session)
                    .questionOrder(currentQA.getQuestionOrder())
                    .followUpOrder(currentQA.getFollowUpOrder() + 1)
                    .questionText(followUpQuestionText)
                    .build();
            interviewQARepository.save(followUpQA);

            return new InterviewDto.NextResponse(followUpQuestionText, false);
        } else {
            int nextQuestionOrder = currentQA.getQuestionOrder() + 1;
            if (nextQuestionOrder > MAX_MAIN_QUESTIONS) {
                String closingPrompt = promptService.createClosingPrompt(conversationHistory);
                String closingRemark = geminiService.generateQuestion(closingPrompt);
                session.complete();
                return new InterviewDto.NextResponse(closingRemark, true);
            } else {
                InterviewQA nextMainQA = interviewQARepository
                        .findByInterviewSession_IdAndQuestionOrderAndFollowUpOrder(session.getId(), nextQuestionOrder, 0)
                        .orElseThrow(() -> new IllegalStateException("다음 주요 질문을 찾을 수 없습니다."));
                return new InterviewDto.NextResponse(nextMainQA.getQuestionText(), false);
            }
        }
    }


    //markdown 표시가 있을경우 제거해줌.
    private String sanitizeJsonResponse(String rawResponse) {
        String sanitized = rawResponse.trim();
        if (sanitized.startsWith("```json")) {
            sanitized = sanitized.substring(7);
        } else if (sanitized.startsWith("```")) {
            sanitized = sanitized.substring(3);
        }
        if (sanitized.endsWith("```")) {
            sanitized = sanitized.substring(0, sanitized.length() - 3);
        }
        return sanitized.trim();
    }

    //꼬리질문이 필요한지 체크
    private boolean shouldAskFollowUp(List<String> conversationHistory) {
        if (conversationHistory.size() < 2) return false;

        String evaluationPrompt = promptService.createAnswerEvaluationPrompt(conversationHistory);
        String evaluationResult = geminiService.generateQuestion(evaluationPrompt);
        return evaluationResult.contains("꼬리질문 필요");
    }

    private List<String> buildConversationHistory(List<InterviewQA> allQAs) {
        return allQAs.stream()
                .flatMap(qa -> {
                    List<String> pair = new ArrayList<>();
                    pair.add(qa.getQuestionText());
                    if (qa.getAnswerText() != null && !qa.getAnswerText().isEmpty()) {
                        pair.add(qa.getAnswerText());
                    }
                    return pair.stream();
                })
                .collect(Collectors.toList());
    }

    private String formatReviewsForPrompt(List<CompanyReview> reviews) {
        if (reviews.isEmpty()) return "이 회사에 대한 면접 데이터가 없습니다.";
        return reviews.stream()
                .map(review -> String.format("- 난이도: %s, 질문: [%s], 후기: %s",
                        review.getDifficulty(),
                        review.getInterviewQuestions() != null ? review.getInterviewQuestions().replace("\n", " ") : "N/A",
                        review.getSummary() != null ? review.getSummary().replace("\n", " ") : "N/A"
                ))
                .limit(20)
                .collect(Collectors.joining("\n"));
    }
}