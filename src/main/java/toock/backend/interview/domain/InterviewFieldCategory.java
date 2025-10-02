package toock.backend.interview.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.stream.Stream;

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
    public static InterviewFieldCategory from(String value) {
        if (value == null) {
            return null;
        }

        for (InterviewFieldCategory category : values()) {
            if (category.getDbValue().equalsIgnoreCase(value)) {
                return category;
            }
        }

        try {
            return InterviewFieldCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}