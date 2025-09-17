package toock.backend.interview.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * 면접 필터링을 위한 3대 직군 카테고리
 */
@Getter
public enum InterviewFieldCategory {
    DEVELOPMENT("개발"),
    DATA("데이터"),
    RND("연구개발");

    private final String dbValue;

    InterviewFieldCategory(String dbValue) {
        this.dbValue = dbValue;
    }

    @JsonCreator
    public static InterviewFieldCategory from(String dbValue) {
        return Stream.of(InterviewFieldCategory.values())
                .filter(category -> category.getDbValue().equals(dbValue))
                .findFirst()
                .orElse(null); // 일치하는 값이 없으면 null 반환 (또는 예외 발생)
    }
}