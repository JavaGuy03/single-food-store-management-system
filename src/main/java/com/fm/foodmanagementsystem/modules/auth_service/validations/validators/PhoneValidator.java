package com.fm.foodmanagementsystem.modules.auth_service.validations.validators;

import com.fm.foodmanagementsystem.modules.auth_service.validations.constraints.PhoneConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class PhoneValidator implements ConstraintValidator<PhoneConstraint, String> {
    private int length;

    @Override
    public void initialize(PhoneConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        length = constraintAnnotation.length();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (Objects.isNull(value) || value.isBlank()) return true;

        // Xóa code thừa, tập trung vào regex
        // Regex giải thích:
        // ^0       : Bắt buộc bắt đầu bằng số 0
        // [35789]  : Số thứ 2 phải là 3, 5, 7, 8 hoặc 9 (các đầu số nhà mạng VN)
        // \\d{...} : Các số còn lại (length - 2)
        // $        : Kết thúc chuỗi

        String phoneRegex = "^0[35789]\\d{" + (length - 2) + "}$";

        return value.matches(phoneRegex);
    }
}
