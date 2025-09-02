package toock.backend.interview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class GeminiDto {

    /**
     * Gemini API에 보낼 요청 DTO
     */
    @Getter
    @AllArgsConstructor
    public static class Request {
        private List<Content> contents;

        // 프롬프트 텍스트를 API 요청 형식에 맞게 변환하는 정적 메소드
        public static Request from(String text) {
            Part part = new Part(text);
            Content content = new Content(List.of(part));
            return new Request(List.of(content));
        }
    }

    /**
     * Gemini API로부터 받을 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    public static class Response {
        @JsonProperty("candidates")
        private List<Candidate> candidates;

        // 중첩된 JSON 구조에서 최종 텍스트만 깔끔하게 추출하는 편의 메소드
        public String getGeneratedText() {
            if (candidates == null || candidates.isEmpty() || candidates.get(0).getContent() == null ||
                    candidates.get(0).getContent().getParts() == null || candidates.get(0).getContent().getParts().isEmpty()) {
                return "죄송합니다. 답변을 생성하는 데 문제가 발생했습니다.";
            }
            return candidates.get(0).getContent().getParts().get(0).getText();
        }
    }

    // --- JSON 구조에 맞춘 내부 클래스들 ---
    @Getter @AllArgsConstructor private static class Content { private List<Part> parts; }
    @Getter @AllArgsConstructor private static class Part { private String text; }
    @Getter @NoArgsConstructor private static class Candidate { @JsonProperty("content") private Content content; }
}