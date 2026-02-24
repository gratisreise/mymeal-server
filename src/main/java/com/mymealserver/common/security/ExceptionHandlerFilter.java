package com.mymealserver.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.response.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException e) {
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, ErrorCode.TOKEN_INVALID);
        } catch (Exception e) {
            setErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode, Exception ex)
        throws IOException {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType("application/json; charset=UTF-8");
        ErrorResponse errorResponse = ErrorResponse.unknown(ex);

        generateBody(response, errorResponse);
    }

    private void setErrorResponse(HttpStatus status, HttpServletResponse response, ErrorCode errorCode)
        throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json; charset=UTF-8");
        ErrorResponse errorResponse = ErrorResponse.from(errorCode);

        generateBody(response, errorResponse);
    }

    private void generateBody(HttpServletResponse response, ErrorResponse errorResponse)
        throws IOException {
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}
