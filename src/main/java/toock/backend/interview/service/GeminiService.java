package toock.backend.interview.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import toock.backend.interview.dto.GeminiDto;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class GeminiService {

    private final WebClient webClient;
    private final String apiKey;

    public GeminiService(WebClient.Builder webClientBuilder, @Value("${gemini.api.key}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com").build();
        this.apiKey = apiKey;
    }

    /**
     * 프롬프트를 받아 Gemini API를 호출하고, 생성된 텍스트 응답을 반환합니다.
     * @param prompt Gemini API에 전달할 전체 프롬프트 문자열
     * @return 생성된 질문 또는 답변 텍스트
     */
    public String generateQuestion(String prompt) {
        GeminiDto.Request requestBody = GeminiDto.Request.from(prompt);

        try {
            GeminiDto.Response response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            // 최신 모델인 gemini-1.5-flash 사용
                            .path("/v1beta/models/gemini-2.5-flash:generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GeminiDto.Response.class)
                    .block(); // 비동기 호출을 동기 방식으로 기다립니다.

            if (response != null) {
                return response.getGeneratedText().trim(); // 응답 양 끝의 공백 제거
            }
        } catch (Exception e) {
            log.error("Gemini API 호출 중 오류 발생: {}", e.getMessage());
            return "API 호출에 실패했습니다. 잠시 후 다시 시도해주세요.";
        }
        return "알 수 없는 오류가 발생했습니다.";
    }
}