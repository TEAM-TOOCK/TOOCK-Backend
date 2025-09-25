package toock.backend.global.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponseDto<T> {

    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> CommonResponseDto<T> success(T data) {
        return CommonResponseDto.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message("요청에 성공하였습니다.")
                .data(data)
                .build();
    }

    public static <T> CommonResponseDto<T> fail(String code, String message) {
        return CommonResponseDto.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }
}
