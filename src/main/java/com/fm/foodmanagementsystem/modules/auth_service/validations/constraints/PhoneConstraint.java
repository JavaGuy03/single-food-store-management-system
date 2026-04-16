package com.fm.foodmanagementsystem.modules.auth_service.validations.constraints;

import com.fm.foodmanagementsystem.modules.auth_service.validations.validators.PhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
public @interface PhoneConstraint {
    String message() default "Invalid phone number";

    int length();

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
