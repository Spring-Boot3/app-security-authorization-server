package com.romlab.app_security.controllers;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/oidc")
public class OidcLogoutEndpoint {

    @GetMapping("/logout")
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication,
            @RequestParam(value = "post_logout_redirect_uri", required = false) String postLogoutRedirectUri
    ) throws IOException {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        // Elimina cookies manualmente (si aplica)
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        // Redirecciona después del logout
        response.sendRedirect(Optional.ofNullable(postLogoutRedirectUri).orElse("http://localhost:4200/"));
    }
}
