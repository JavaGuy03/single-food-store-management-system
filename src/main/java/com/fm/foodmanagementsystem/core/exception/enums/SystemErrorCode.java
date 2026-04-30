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
    UNCATEGORIZED_EXCEPTION(1001, "Unknown exception!", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1002, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    USER_EXISTED(1003, "User already existed!", HttpStatus.CONFLICT),
    USER_NOT_EXISTED(1004, "User not existed!", HttpStatus.NOT_FOUND),
    DATA_NOT_FOUND(1005, "Data not found!", HttpStatus.NOT_FOUND),
    INVALID_PARAMETER(1006, "Invalid parameter!", HttpStatus.BAD_REQUEST),
    DATA_IS_IN_USE(1007, "Data is already in use!", HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR(1008, "Internal server error!", HttpStatus.INTERNAL_SERVER_ERROR),
    FOOD_UNAVAILABLE(1009, "This food item is currently unavailable!", HttpStatus.BAD_REQUEST),
    COUPON_EXPIRED(1010, "This coupon has expired!", HttpStatus.BAD_REQUEST),
    COUPON_USAGE_LIMIT(1011, "This coupon has reached its usage limit!", HttpStatus.BAD_REQUEST),
    COUPON_MIN_ORDER(1012, "Order total does not meet the minimum requirement for this coupon!", HttpStatus.BAD_REQUEST),
    COUPON_ALREADY_EXISTS(1013, "A coupon with this code already exists!", HttpStatus.CONFLICT),
    INVALID_ORDER_STATUS_TRANSITION(1014, "Invalid order status transition!", HttpStatus.BAD_REQUEST),
    USER_DISABLED(1015, "This account has been disabled!", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_ACTION(1016, "You are not authorized to perform this action!", HttpStatus.FORBIDDEN),

    ;
    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
