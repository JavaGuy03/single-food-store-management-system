package com.fm.foodmanagementsystem.core.exception.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ValidationErrorCode {
    UNKNOWN_VALIDATOR(100, "Unknown Validator", HttpStatus.BAD_REQUEST),
    NOT_BLANK(101, "This field must not be blanked", HttpStatus.BAD_REQUEST),
    NOT_NULL(102, "This field must not null", HttpStatus.BAD_REQUEST),
    NOT_EMPTY(103, "This field must not be empty", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(104, "Email is invalid", HttpStatus.BAD_REQUEST),
    INVALID_PHONE(105, "Phone number is invalid", HttpStatus.BAD_REQUEST),
    INVALID_OTP(106, "OTP is invalid", HttpStatus.BAD_REQUEST),
    ;

    int code;
    String message;
    HttpStatus status;
}
