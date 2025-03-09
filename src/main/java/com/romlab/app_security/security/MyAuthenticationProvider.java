package com.romlab.app_security.security;

import com.romlab.app_security.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@AllArgsConstructor
public class MyAuthenticationProvider implements AuthenticationProvider {

    private CustomerRepository customerRepository;
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final var username = authentication.getName();
        final var password = authentication.getCredentials().toString();
        final var customerFromDb = customerRepository.findByEmail(username);
        final var customer = customerFromDb.orElseThrow(() -> new BadCredentialsException("Customer not found"));
        final var passwordHash = customer.getPassword();
        if (passwordEncoder.matches(password, passwordHash)) {
//            final var authorities = Collections.singletonList(new SimpleGrantedAuthority(customer.getRole()));
            final var roles = customer.getRole();
            final var authorities = roles
                    .stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName()))
                    .toList();
            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        } else {
            throw new BadCredentialsException("Bad credentials");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
