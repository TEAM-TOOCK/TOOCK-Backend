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

    /**
     * 면접 전체 대화 내용을 바탕으로 지원자를 평가하기 위한 프롬프트를 생성합니다.
     * 기술 역량, 소프트스킬, 문제 해결력, 성장 가능성, 그리고 종합 요약을 포함합니다.
     * 응답은 반드시 JSON 형식이어야 합니다.
     * @param conversationHistory 질문과 답변이 번갈아 담긴 전체 대화 기록 리스트
     * @return Gemini API에 전달할 면접 평가용 프롬프트
     */
    public String createInterviewEvaluationPrompt(List<String> conversationHistory) {
        String history = formatConversationHistory(conversationHistory);
        return """
            당신은 IT 기업의 숙련된 면접관으로서, 주어진 평가 기준에 따라 지원자를 객관적으로 평가하는 역할을 맡았습니다.
            아래에 제공된 **'평가 기준 및 척도'**와 **'면접 시나리오'**를 바탕으로, 지원자의 역량을 4가지 핵심 항목에 대해 1점에서 5점까지 평가하고, 각 점수에 대한 구체적인 근거를 제시해야 합니다. 평가는 반드시 제시된 기준과 척도 정의에만 입각하여 논리적으로 이루어져야 합니다.

            ### 1. 평가 기준 및 척도
            가. 문제 해결 능력 (Problem Solving)
            정의: 기술을 활용하여 복잡하고 모호한 문제를 분석하고, 최적의 해결책을 찾아내는 능력
            5점 (탁월): 문제의 핵심을 정확히 파악하고, 여러 해결책의 장단점(Trade-off)을 명확히 비교 분석하여 최적의 방안을 논리적으로 제시함. 시간/공간 복잡도와 엣지 케이스까지 완벽하게 고려함.
            4점 (우수): 체계적인 접근으로 문제를 해결하며, 효율적인 해결책을 제시함. 약간의 힌트가 주어지면 엣지 케이스나 최적화 방안을 찾아냄.
            3점 (보통): 문제는 해결하지만, 비효율적인 방식이거나 엣지 케이스를 일부 놓침. 일반적인 수준의 해결 능력을 보임.
            2점 (미흡): 문제 해결에 많은 힌트가 필요하며, 해결 과정이 논리적이지 않고 산만함.
            1점 (개선 필요): 문제 자체를 이해하는 데 어려움을 겪거나, 해결책을 거의 제시하지 못함.

            나. 기술 전문성 (Technical Expertise)
            정의: CS 기본 지식과 주력 기술 스택에 대한 깊이 있는 이해 및 시스템 설계 능력
            5점 (탁월): 기술의 내부 동작 원리까지 깊이 있게 이해하며, 기술 선택의 이유를 시스템 전체 관점에서 설명함. 확장성과 안정성을 고려한 시스템 설계가 가능함.
            4점 (우수): 주력 기술 스택을 능숙하게 사용하며, 관련된 CS 지식이 탄탄함. 주어진 요구사항에 맞는 합리적인 시스템 구성이 가능함.
            3점 (보통): 기술을 사용하여 기능 구현은 가능하지만, 내부 동작 원리에 대한 이해는 부족함. 기본적인 웹 아키텍처를 이해하고 있음.
            2점 (미흡): 기술 개념에 대한 설명이 부정확하며, 지식이 단편적이고 피상적임.
            1점 (개선 필요): 직무 수행에 필요한 기본적인 기술 지식이 현저히 부족함.

            다. 협업 및 소통 능력 (Collaboration & Communication)
            정의: 팀의 목표 달성을 위해 명확하게 소통하고, 동료와 건설적인 관계를 맺으며 시너지를 내는 능력
            5점 (탁월): 자신의 생각을 명확하고 논리적으로 전달하며, 다른 의견을 경청하고 존중함. 갈등 상황에서 건설적인 해결책을 제시하며 팀에 긍정적인 영향을 줌.
            4점 (우수): 자신의 의견을 잘 설명하고, 동료와 원활하게 협업함. 코드 리뷰 등에서 건전한 피드백을 주고받을 수 있음.
            3점 (보통): 기본적인 의사소통과 협업은 가능하지만, 자신의 생각을 설득력 있게 전달하는 능력은 다소 부족함.
            2점 (미흡): 소통이 다소 일방적이거나, 질문의 의도를 잘 파악하지 못하는 경향이 있음.
            1점 (개선 필요): 방어적인 태도를 보이거나, 효과적인 의사소통이 어려워 협업이 힘들 것으로 판단됨.

            라. 성장 잠재력 (Growth Potential)
            정의: 현재 역량에 안주하지 않고, 자기 주도적으로 학습하며 실패를 통해 성장하는 태도와 의지
            5점 (탁월): 새로운 기술에 대한 강한 호기심과 학습 의지를 보이며, 실패 경험을 성장의 발판으로 삼는 성숙한 태도를 가짐. 팀의 기술 문화 개선에 기여할 가능성이 높음.
            4점 (우수): 꾸준히 학습하는 습관을 가지고 있으며, 자신의 부족한 점을 개선하려는 노력이 보임. 피드백에 대해 열린 태도를 보임.
            3점 (보통): 성장에 대한 일반적인 의지는 있으나, 구체적인 노력이나 경험은 부족함.
            2점 (미흡): 기술적 호기심이나 학습에 대한 동기가 부족해 보이며, 성장이 정체되어 있을 가능성이 있음.
            1점 (개선 필요): 변화를 꺼리거나, 자신의 방식만을 고수하려는 태도를 보임.
            
            면접관으로서의 냉철한 관점으로 지원자의 답변 내용만을 기준으로 판단합니다.
            **절대 가상의 경험을 추가하여 답변하지 않습니다.**
            
            각 항목은 1점부터 5점까지 점수를 부여하고, 총점 (totalScore)은 각 항목 점수의 평균으로 계산합니다. (소수점은 버립니다.)
            또한, 각 평가 항목별 점수 부여 근거와 전체적인 총평 (overallAssessment)을 상세하게 작성해주세요. **총평(summary)은 500자 이내로 간략하게 요약합니다.** 총평에는 지원자에 대한 전반적인 평가와 강점, 약점, 채용 추천 여부 등을 간략히 요약합니다.
            
            **매우 중요**: 당신의 응답은 반드시 아래와 같은 JSON 형식이어야 합니다.
            다른 부가적인 설명이나 markdown(`json ... `) 없이 순수한 JSON만 응답해야 합니다.
            
            [응답 형식 예시]
            {
                "totalScore": 4,
                "problemSolvingScore": 4,
                "technicalExpertiseScore": 5,
                "collaborationCommunicationScore": 4,
                "growthPotentialScore": 3,
                "summary": "지원자는 Spring Boot를 활용한 N+1 쿼리 문제 해결 경험에서 뛰어난 문제 해결 능력과 기술 전문성을 보여주었습니다. 팀원들과의 협업 과정도 원활했으며, 적극적인 학습 의지를 엿볼 수 있었습니다. 전반적으로 우수한 역량을 갖추고 있으며, 특히 백엔드 기술 스택에 대한 깊은 이해를 바탕으로 안정적인 시스템 설계 및 구현이 가능할 것으로 판단됩니다. 다만, 추가적인 경험을 통해 더욱 다양한 상황에서의 문제 해결 능력과 주도적인 학습 경험을 쌓는다면 더욱 성장할 것으로 기대됩니다. 채용을 추천합니다."
            }
            
            [전체 대화 내용]:
            %s
            """.formatted(history);
    }
}