package com.romlab.app_security.security.filters;

import com.romlab.app_security.services.JWTService;
import com.romlab.app_security.services.JWTUserDetailService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class JWTValidationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final JWTUserDetailService jwtUserDetailService;

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_HEADER_BEARER = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final var requestTokenHeaders = request.getHeader(AUTHORIZATION_HEADER);
        String username = null;
        String jwt = null;

        if(Objects.nonNull(requestTokenHeaders) && requestTokenHeaders.startsWith(AUTHORIZATION_HEADER_BEARER) ) {
            jwt = requestTokenHeaders.substring(7);

            try {
                username = jwtService.getUserNameFromToken(jwt);
            } catch (IllegalArgumentException | ExpiredJwtException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }

        if(Objects.nonNull(username) && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {
            final var userDetails = jwtUserDetailService.loadUserByUsername(username);

            if (jwtService.validateToken(jwt, userDetails)) {
                var usernameAndPassAuthToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                usernameAndPassAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernameAndPassAuthToken);
            }
        }

        filterChain.doFilter(request, response);

    }

}
