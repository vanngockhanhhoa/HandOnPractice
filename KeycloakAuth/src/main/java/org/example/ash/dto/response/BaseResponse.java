package org.example.ash.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> {

    private final int status;
    private final String error;
    private final T data;

    // ── Static factories ───────────────────────────────────────────────────────

    public static <T> BaseResponse<T> ok(T data) {
        return BaseResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> created(T data) {
        return BaseResponse.<T>builder()
                .status(HttpStatus.CREATED.value())
                .data(data)
                .build();
    }

    public static BaseResponse<Void> error(HttpStatus httpStatus, String message) {
        return BaseResponse.<Void>builder()
                .status(httpStatus.value())
                .error(message)
                .build();
    }

    public static BaseResponse<Void> badRequest(String message) {
        return error(HttpStatus.BAD_REQUEST, message);
    }

    public static BaseResponse<Void> internalError(String message) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}