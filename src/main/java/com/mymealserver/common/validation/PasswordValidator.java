package com.mymealserver.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 비밀번호 유효성 검증 validator
 *
 * <p>검증 규칙:
 * <ul>
 *   <li>길이: 8자 이상, 20자 이하</li>
 *   <li>영문자: 최소 하나 포함</li>
 *   <li>숫자: 최소 하나 포함</li>
 *   <li>특수문자: @$!%*#?& 중 최소 하나 포함</li>
 * </ul>
 */
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
