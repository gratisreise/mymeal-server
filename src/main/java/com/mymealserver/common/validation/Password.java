package com.mymealserver.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 비밀번호 유효성 검증 어노테이션
 *
 * <p>비밀번호는 다음 조건을 만족해야 합니다:
 * <ul>
 *   <li>최소 8자, 최대 20자</li>
 *   <li>최소 하나의 영문자 (대소문자 구분)</li>
 *   <li>최소 하나의 숫자</li>
 *   <li>최소 하나의 특수문자 (@$!%*#?&)</li>
 * </ul>
 */
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
@Documented
public @interface Password {

    String message() default "{validation.password.pattern}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int minLength() default 8;

    int maxLength() default 20;
}
