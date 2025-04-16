package com.romlab.app_security.components;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String ERROR_MESSAGE = "<div class=\"error-message\">Usuario o contraseña incorrectos. Intenta de nuevo.</div>";
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(ERROR_MESSAGE);
        response.getWriter().flush();
    }
}
