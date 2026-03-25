package com.mymealserver.common.validation;

import com.mymealserver.common.annotation.Password;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]+$"
    );

    private int minLength;
    private int maxLength;

    @Override
    public void initialize(Password constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // null은 @NotBlank 등 다른 어노테이션에서 처리
        if (password == null) {
            return true;
        }

        // 길이 검증
        if (password.length() < minLength || password.length() > maxLength) {
            return false;
        }

        // 패턴 검증 (영문, 숫자, 특수문자 조합)
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
