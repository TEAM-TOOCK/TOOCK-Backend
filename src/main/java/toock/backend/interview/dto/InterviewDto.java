package toock.backend.interview.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import toock.backend.user.domain.Field; // User 도메인의 Field Enum 사용

public class InterviewDto {

    // 면접 시작 요청 DTO
    @Getter @Setter @NoArgsConstructor
    public static class StartRequest {
        private Long memberId;      // 면접을 진행할 멤버 ID
        private String companyName; // 면접 볼 회사 이름
        private Field field;        // 면접 볼 직무 분야
    }

    // 면접 시작 응답 DTO
    @Getter @Setter @NoArgsConstructor
    public static class StartResponse {
        private Long interviewSessionId;
        private String questionText;

        public StartResponse(Long interviewSessionId, String questionText) {
            this.interviewSessionId = interviewSessionId;
            this.questionText = questionText;
        }
    }

    // 다음 질문 요청 DTO
    @Getter @Setter @NoArgsConstructor
    public static class NextRequest {
        private Long interviewSessionId;
        private String answerText;
        private String s3Url;
    }

    // 다음 질문 응답 DTO
    @Getter @Setter @NoArgsConstructor
    public static class NextResponse {
        private String questionText;
        private boolean isFinished; // 면접 종료 여부 플래그

        public NextResponse(String questionText, boolean isFinished) {
            this.questionText = questionText;
            this.isFinished = isFinished;
        }
    }
}