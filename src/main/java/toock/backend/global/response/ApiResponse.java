package com.example.runway.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ApiResponse<T> {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private T data;

    // 성공 (기본 200)
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("200")
                .message("요청에 성공하였습니다.")
                .data(data)
                .build();
    }

    // 성공 (201/202 등 커스텀 상태)
    public static <T> ApiResponse<T> success(int code, T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(String.valueOf(code))
                .message(message)
                .data(data)
                .build();
    }

    // 실패 (표준 패턴)
    public static <T> ApiResponse<T> fail(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}
