package RedSource.entities.utils;

import RedSource.entities.response.ErrorResponse;
import RedSource.entities.response.SuccessResponse;
import org.springframework.http.HttpStatus;

public class ResponseUtils {

    public static <T> SuccessResponse<T> buildSuccessResponse(HttpStatus status, String message) {
        return SuccessResponse.<T>builder()
                .statusCode(status.value())
                .message(message)
                .build();
    }

    public static <T> SuccessResponse<T> buildSuccessResponse(HttpStatus status, String message, T data) {
        return SuccessResponse.<T>builder()
                .statusCode(status.value())
                .message(message)
                .data(data)
                .build();
    }

    public static ErrorResponse buildErrorResponse(HttpStatus status, String message) {
        return ErrorResponse.builder()
                .statusCode(status.value())
                .message(message)
                .build();
    }
}
