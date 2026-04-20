package com.fm.foodmanagementsystem.core.exception.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum SystemErrorCode {
    UNCATEGORIZED_EXCEPTION(1001, "Unknow exception!", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1002, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    USER_EXISTED(1003, "User already existed!", HttpStatus.CONFLICT),
    USER_NOT_EXISTED(1004, "User not existed!", HttpStatus.NOT_FOUND),
    DATA_NOT_FOUND(1005, "Data not found!", HttpStatus.NOT_FOUND),

    ;
    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
