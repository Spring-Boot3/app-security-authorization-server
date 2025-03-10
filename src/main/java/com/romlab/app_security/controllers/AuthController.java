package com.romlab.app_security.controllers;

import com.romlab.app_security.entities.JWTRequest;
import com.romlab.app_security.entities.JWTResponse;
import com.romlab.app_security.services.JWTService;
import com.romlab.app_security.services.JWTUserDetailService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUserDetailService jwtUserDetailService;
    private final JWTService jwtService;

    @PostMapping
    public ResponseEntity<?> postToken(@RequestBody JWTRequest jwtRequest){
        authentication(jwtRequest);
        final var userDetails = jwtUserDetailService.loadUserByUsername(jwtRequest.getUsername());
        System.out.println("-->>" + userDetails);
        final var token = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new JWTResponse(token));
    }

    private void authentication(JWTRequest jwtRequest){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(jwtRequest.getUsername(), jwtRequest.getPassword())
            );
        } catch (BadCredentialsException | DisabledException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

}
