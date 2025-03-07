package com.romlab.app_security.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

public class CsrfCookieFilter extends OncePerRequestFilter {

    /* Esto lo que hara es generar el token y colocarlo en los headers */

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        var csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (Objects.nonNull(csrfToken.getHeaderName())){
            response.addHeader(csrfToken.getHeaderName(), csrfToken.getToken());
            filterChain.doFilter(request, response);
        }

    }
}
