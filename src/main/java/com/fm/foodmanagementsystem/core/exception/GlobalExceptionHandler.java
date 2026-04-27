package com.fm.foodmanagementsystem.core.exception;

import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.core.exception.enums.ValidationErrorCode;
import com.fm.foodmanagementsystem.core.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // 1. Handle Custom Business Exception (AppException, ProductException...)
    @ExceptionHandler(value = SystemException.class)
    public ResponseEntity<ApiResponse<Object>> handlingAppException(SystemException systemException) {
        SystemErrorCode errorCode = systemException.getErrorCode();

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    // 2. Handle Validation Exception (@NotBlank, @Email...)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handlingValidationException(MethodArgumentNotValidException methodArgumentNotValidException){
        Map<String, String> errors = new HashMap<>();

        for(FieldError error : methodArgumentNotValidException.getBindingResult().getFieldErrors()) {
            String field = error instanceof FieldError ? error.getField() : "unknown error";
            String errorCodeStr = error.getDefaultMessage(); // String key: "INVALID_EMAIL"

            ValidationErrorCode validationCode = getValidationCode(errorCodeStr);
            String message = validationCode.getMessage();

            // Unwrap logic to get attributes (min, max...)
            try {
                ConstraintViolation<?> violation = error.unwrap(ConstraintViolation.class);
                Map<String, Object> attributes = violation.getConstraintDescriptor().getAttributes();
                message = mapAttributesToMessage(message, attributes);
            } catch (Exception exception) {
                log.debug("Cannot unwrap constraint violation", exception);
            }

            errors.put(field, message);
        }

        return ResponseEntity
                .badRequest()
                .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors));
    }

    // 3. Handle Spring Security 403 Forbidden
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handlingAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>(HttpStatus.FORBIDDEN.value(), "Access Denied: You do not have permission to access this resource", null));
    }

    // 4. Handle 404 Not Found
    @ExceptionHandler(value = NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handlingNotFoundException(NoResourceFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Resource not found: " + exception.getResourcePath(), null));
    }

    // 5. Handle Bad Request (Type mismatch, unreadable JSON, etc)
    @ExceptionHandler(value = {HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<Object>> handlingBadRequestException(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Invalid request format or parameters", null));
    }

    // 6. Handle Uncategorized Exception (Lỗi 500, NullPointer, DB Error...)
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Object>> handlingUnknownException(Exception exception) {
        log.error("Uncategorized error: ", exception); // Nên log lỗi ra để debug

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        SystemErrorCode errorCode = SystemErrorCode.UNCATEGORIZED_EXCEPTION;

        apiResponse.setCode(errorCode.getCode());
        // Có thể trả về message gốc của exception để debug, hoặc ẩn đi nếu muốn bảo mật, đoạn code dưới phải che nếu như deploy thật
        //apiResponse.setMessage(exception.getMessage() != null ? exception.getMessage() : errorCode.getMessage());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    // --- Helper Methods ---
    private ValidationErrorCode getValidationCode(String errorCode) {
        try {
            return ValidationErrorCode.valueOf(errorCode);
        } catch (IllegalArgumentException | NullPointerException e) {
            return ValidationErrorCode.UNKNOWN_VALIDATOR;
        }
    }

    private String mapAttributesToMessage(String template, Map<String, Object> attributes) {
        String result = template;
        for(Map.Entry<String, Object> entry : attributes.entrySet()) {
            String placeHolder = "{" + entry.getKey() + "}";
            if (result.contains(placeHolder)) {
                result = result.replace(placeHolder, String.valueOf(entry.getValue()));
            }
        }
        return result;
    }
}
