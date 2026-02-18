package com.mymealserver.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject the authenticated member's ID directly into controller methods.
 * Use this annotation on Long parameters to receive the member ID without manual extraction.
 *
 * <p>Example usage:
 * <pre>
 * &#64;PostMapping("/logout")
 * public ResponseEntity&lt;SuccessResponse&lt;Void&gt;&gt; logout(
 *     &#64;AuthenticatedMember Long memberId,
 *     &#64;Valid &#64;RequestBody RefreshTokenRequest request
 * ) {
 *     authService.logout(memberId, request.refreshToken());
 *     return SuccessResponse.toOk(null);
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthenticatedMember {

    /**
     * Expression for advanced SpEL support (reserved for future use).
     * @return expression string
     */
    String expression() default "";

    /**
     * Alias for expression attribute (reserved for future use).
     * @return expression string
     */
    String value() default "";
}
