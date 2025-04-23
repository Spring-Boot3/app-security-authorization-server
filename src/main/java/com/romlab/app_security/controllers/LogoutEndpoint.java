package com.romlab.app_security.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class LogoutEndpoint {

    @GetMapping("/logouts")
    public String logout(
            @RequestParam(value = "post_logout_redirect_uri", required = false) String postLogoutRedirectUri,
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        if (authentication != null) { new SecurityContextLogoutHandler().logout(request, response, authentication); }
        return "redirect:" + postLogoutRedirectUri;
    }
}
