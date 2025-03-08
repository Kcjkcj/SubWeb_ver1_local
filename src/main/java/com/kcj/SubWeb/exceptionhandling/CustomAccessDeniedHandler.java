package com.kcj.SubWeb.exceptionhandling;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;

public class CustomAccessDeniedHandler implements AccessDeniedHandler { //클라이언트에 403상황에 대해서 해당 상황을 자세하게 리턴해주기 위한 함수 ex(postman)
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        LocalDateTime localDateTime = LocalDateTime.now();
        String message = (accessDeniedException != null && accessDeniedException.getMessage() != null) ?
                accessDeniedException.getMessage() : "Authorization Failed";
        String path = request.getRequestURI();
        response.setHeader("Subweb-denied-reason","Authorization failed"); //한국어 인코딩 안됨 UTF-8이라서
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        // JSON 응답 구축
        String jsonResponse =
                String.format("{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"%s\", " +
                        "\"message\": \"%s\", \"path\": \"%s\"}",
                        localDateTime,HttpStatus.FORBIDDEN.value(),HttpStatus.FORBIDDEN.getReasonPhrase(),
                        message,path);
        response.getWriter().write(jsonResponse);

    }
}
