package toock.backend.interview.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PromptService {

    /**
     * 면접 시작 시, 주요 질문 5개를 한 번에 생성하기 위한 프롬프트를 생성합니다.
     * @param contextData DB에서 조회 후 가공된 회사 면접 후기 데이터
     * @param field 면접을 진행할 직무 분야 (예: "BACKEND")
     * @return Gemini API에 전달할 주요 질문 생성용 프롬프트
     */
    public String createMainQuestionsPrompt(String contextData, String field) {
        return """
            당신은 %s 직무의 기술 면접관입니다.
            당신이 면접 볼 회사는 아래와 같은 면접 후기 데이터가 있습니다. 이 데이터를 참고하여 회사의 면접 스타일(난이도, 질문 유형)에 맞춰 면접을 진행해주세요.
            
            지원자의 기술 역량, 프로젝트 경험, 문제 해결 능력, 협업 능력, 성장 가능성을 종합적으로 평가할 수 있는 **핵심적인 주요 질문 5개**를 생성해주세요.
            
            **매우 중요**: 당신의 응답은 반드시 아래와 같은 JSON 배열 형식이어야 합니다.
            다른 부가적인 설명이나 markdown(`json ... `) 없이 순수한 JSON 배열만 응답해야 합니다.
            
            [응답 형식 예시]
            ["첫 번째 질문 텍스트입니다.", "두 번째 질문 텍스트입니다.", "세 번째 질문 텍스트입니다.", "네 번째 질문 텍스트입니다.", "다섯 번째 질문 텍스트입니다."]

            [회사 면접 데이터]
            %s
            """.formatted(field, contextData);
    }

    /**
     * 사용자의 답변을 평가하여 꼬리 질문 필요 여부를 판단하기 위한 프롬프트를 생성합니다.
     * @param conversationHistory 질문과 답변이 번갈아 담긴 전체 대화 기록 리스트
     * @return Gemini API에 전달할 평가용 프롬프트
     */
    public String createAnswerEvaluationPrompt(List<String> conversationHistory) {
        // 대화 기록에서 마지막 질문과 답변을 안전하게 추출
        String lastQuestion = conversationHistory.get(conversationHistory.size() - 2);
        String lastAnswer = conversationHistory.get(conversationHistory.size() - 1);

        return """
            당신은 지원자의 답변을 평가하는 면접관입니다. 아래는 면접관의 질문과 지원자의 답변입니다.
            [질문]: %s
            [답변]: %s
            이 답변이 충분히 구체적이고 경험에 기반하여 상세하게 설명되었다면 "다음 질문"이라고만 대답해주세요.
            만약 답변이 추상적이거나, 특정 기술 용어에 대해 더 깊게 파고들 여지가 있거나, 너무 짧다면 "꼬리질문 필요"라고만 대답해주세요.
            오직 "다음 질문" 또는 "꼬리질문 필요" 둘 중 하나로만 대답해야 합니다.
            """.formatted(lastQuestion, lastAnswer);
    }

    /**
     * 질문-답변 리스트를 Gemini가 이해하기 쉬운 대화록 형식의 문자열로 변환합니다.
     * @param conversationHistory 질문과 답변이 번갈아 담긴 리스트
     * @return "Q: ...\nA: ..." 형태의 전체 대화록 문자열
     */
    private String formatConversationHistory(List<String> conversationHistory) {
        StringBuilder formattedHistory = new StringBuilder();
        for (int i = 0; i < conversationHistory.size(); i++) {
            if (i % 2 == 0) { // 짝수 인덱스는 질문
                formattedHistory.append("Q: ").append(conversationHistory.get(i)).append("\n");
            } else { // 홀수 인덱스는 답변
                formattedHistory.append("A: ").append(conversationHistory.get(i)).append("\n\n");
            }
        }
        return formattedHistory.toString();
    }

    /**
     * 꼬리 질문 생성을 위한 프롬프트를 생성합니다.
     */
    public String createFollowUpPrompt(List<String> conversationHistory) {
        String history = formatConversationHistory(conversationHistory);
        return """
            당신은 기술 면접관입니다. 아래는 지금까지의 대화 내용입니다.
            [대화 내용]:
            %s
            
            방금 지원자가 한 마지막 답변에 대해, 더 구체적인 내용을 확인하기 위한 꼬리 질문을 하나만 생성해주세요.
            질문은 간결하고 명확해야 합니다.
            """.formatted(history);
    }

    /**
     * 면접 마무리를 위한 프롬프트를 생성합니다.
     */
    public String createClosingPrompt(List<String> conversationHistory) {
        String history = formatConversationHistory(conversationHistory);
        return """
            당신은 기술 면접을 성공적으로 마친 면접관입니다. 아래 대화 내용 전체를 참고하여, 면접을 마무리하는 자연스러운 멘트를 생성해주세요.
            지원자에게 감사 인사를 전하고, 마지막으로 궁금한 점이 있는지 질문하며 끝맺어주세요.
            [전체 대화 내용]:
            %s
            """.formatted(history);
    }
}