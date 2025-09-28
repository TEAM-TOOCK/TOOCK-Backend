package toock.backend.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import toock.backend.company.domain.Company;
import toock.backend.company.domain.CompanyReview;
import toock.backend.company.repository.CompanyRepository;
import toock.backend.company.repository.CompanyReviewRepository;
import toock.backend.interview.domain.InterviewQA;
import toock.backend.interview.domain.InterviewAnalysis;
import toock.backend.interview.domain.InterviewSession;
import toock.backend.interview.dto.InterviewDto;
import toock.backend.interview.dto.InterviewAnalysisResponseDto;
import toock.backend.interview.dto.InterviewEvaluationResult;
import toock.backend.interview.repository.InterviewQARepository;
import toock.backend.interview.repository.InterviewSessionRepository;
import toock.backend.interview.repository.InterviewAnalysisRepository;
import toock.backend.member.domain.Member;
import toock.backend.member.repository.MemberRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final InterviewAnalysisRepository interviewAnalysisRepository;
    private final PromptService promptService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    private static final int MAX_MAIN_QUESTIONS = 3;
    private static final int MAX_FOLLOW_UP_QUESTIONS = 1;
    private static final int MAX_REVIEW_SAMPLES = 20; //



    @Transactional
    public InterviewDto.StartResponse startInterview(InterviewDto.StartRequest request,Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " +memberId));
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

        // 1. Enum의 이름을 문자열로 변환
        String fieldCategory = request.getField().getDbValue();

        // 2. 변환된 문자열로 데이터베이스에서 직접 면접 후기를 조회합니다. (랜덤으로 N개 조회)
        List<CompanyReview> reviews = companyReviewRepository.findRandomByCompanyAndField(
                company.getName(),
                fieldCategory,
                PageRequest.of(0, MAX_REVIEW_SAMPLES)
        );


        log.info("reviews: {}", reviews.toString());

        String contextData = formatReviewsForPrompt(reviews);
        String mainQuestionsPrompt = promptService.createMainQuestionsPrompt(contextData, fieldCategory);
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
    public InterviewDto.NextResponse nextQuestion(InterviewDto.NextRequest request,Long memberId) {
        InterviewSession session = interviewSessionRepository.findById(request.getInterviewSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다. ID: " + request.getInterviewSessionId()));

        if (!session.getMember().getId().equals(memberId)) {
            // 다른 사람의 면접에 접근하려고 하면 에러 발생
            throw new SecurityException("해당 면접에 접근할 권한이 없습니다.");
        }

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

    @Transactional
    public InterviewAnalysisResponseDto evaluateInterview(Long interviewSessionId) {
        InterviewSession session = interviewSessionRepository.findById(interviewSessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다. ID: " + interviewSessionId));

        List<InterviewQA> qas = interviewQARepository.findByInterviewSession_IdOrderByQuestionOrderAscFollowUpOrderAsc(interviewSessionId);
        if (qas.isEmpty()) {
            throw new IllegalArgumentException("해당 세션에 대한 면접 질문-답변 데이터가 없습니다. ID: " + interviewSessionId);
        }

        // 기존에 분석된 내용이 있다면 반환 (멱등성 보장)
        Optional<InterviewAnalysis> existingAnalysis = interviewAnalysisRepository.findByInterviewSessionId(interviewSessionId);
        if (existingAnalysis.isPresent()) {
            return createAnalysisResponseDto(existingAnalysis.get(), existingAnalysis.get().getInterviewSession());
        }

        String evaluationPrompt = promptService.createInterviewEvaluationPrompt(buildConversationHistory(qas));
        String rawResponse = geminiService.generateQuestion(evaluationPrompt);

        String cleanJson = sanitizeJsonResponse(rawResponse);
        InterviewEvaluationResult evaluationResult;
        try {
            evaluationResult = objectMapper.readValue(cleanJson, InterviewEvaluationResult.class);
        } catch (JsonProcessingException e) {
            log.error("Gemini 면접 평가 응답 JSON 파싱 실패. 원본: {}, 정리 후: {}", rawResponse, cleanJson, e);
            throw new IllegalStateException("Gemini로부터 받은 면접 평가 형식이 올바르지 않습니다.");
        }

        String summaryToSave = evaluationResult.getSummary();
        String strengthsToSave = evaluationResult.getStrengths();
        String improvementsToSave = evaluationResult.getImprovements();
        
        log.info("저장될 summary 길이: {}, 내용: {}", summaryToSave != null ? summaryToSave.length() : 0, summaryToSave);
        log.info("저장될 strengths 길이: {}, 내용: {}", strengthsToSave != null ? strengthsToSave.length() : 0, strengthsToSave);
        log.info("저장될 improvements 길이: {}, 내용: {}", improvementsToSave != null ? improvementsToSave.length() : 0, improvementsToSave);

        InterviewAnalysis analysis = InterviewAnalysis.builder()
                .interviewSession(session)
                .score(evaluationResult.getTotalScore())
                .technicalExpertiseScore(evaluationResult.getTechnicalExpertiseScore())
                .collaborationCommunicationScore(evaluationResult.getCollaborationCommunicationScore())
                .problemSolvingScore(evaluationResult.getProblemSolvingScore())
                .growthPotentialScore(evaluationResult.getGrowthPotentialScore())
                .summary(summaryToSave)
                .strengths(strengthsToSave)
                .improvements(improvementsToSave)
                .build();
        interviewAnalysisRepository.save(analysis);

        log.info("면접 분석 결과 저장됨: session_id={}, score={}, technical={}, soft={}, problem={}, growth={}, summary_length={}",
                session.getId(),
                analysis.getScore(),
                analysis.getTechnicalExpertiseScore(),
                analysis.getCollaborationCommunicationScore(),
                analysis.getProblemSolvingScore(),
                analysis.getGrowthPotentialScore(),
                analysis.getSummary() != null ? analysis.getSummary().length() : 0);

        return createAnalysisResponseDto(analysis, analysis.getInterviewSession());
    }

    @Transactional(readOnly = true)
    public InterviewAnalysisResponseDto getInterviewAnalysis(Long interviewSessionId) {
        InterviewAnalysis analysis = interviewAnalysisRepository.findByInterviewSessionId(interviewSessionId)
                .orElseThrow(() -> new IllegalArgumentException("면접 분석을 찾을 수 없습니다. ID: " + interviewSessionId));
        return createAnalysisResponseDto(analysis, analysis.getInterviewSession());
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

    private InterviewAnalysisResponseDto createAnalysisResponseDto(InterviewAnalysis analysis, InterviewSession session) {
        return InterviewAnalysisResponseDto.builder()
                .id(analysis.getId())
                .interviewSessionId(session.getId())
                .score(analysis.getScore())
                .technicalExpertiseScore(analysis.getTechnicalExpertiseScore())
                .collaborationCommunicationScore(analysis.getCollaborationCommunicationScore())
                .problemSolvingScore(analysis.getProblemSolvingScore())
                .growthPotentialScore(analysis.getGrowthPotentialScore())
                .summary(analysis.getSummary())
                .strengths(analysis.getStrengths())
                .improvements(analysis.getImprovements())
                .build();
    }
}