package com.fm.foodmanagementsystem.core.exception;

import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemException extends RuntimeException {
    SystemErrorCode errorCode;

    public SystemException(SystemErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
