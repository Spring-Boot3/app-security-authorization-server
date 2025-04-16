package com.romlab.app_security.components;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HtmxLoginSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {

    private final SavedRequestAwareAuthenticationSuccessHandler delegate = new SavedRequestAwareAuthenticationSuccessHandler();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        ResponseWrapper capturingResponseWrapper = new ResponseWrapper(response);
        this.delegate.onAuthenticationSuccess(request, capturingResponseWrapper, authentication);
        String targetUrl = capturingResponseWrapper.getRedirectUrl();
        if (response.isCommitted()) { return; }
        response.setHeader("HX-Redirect", targetUrl);
    }

    private static class ResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
        private String redirectUrl;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            this.redirectUrl = location;
        }

        public String getRedirectUrl() {
            return redirectUrl;
        }
    }
}
