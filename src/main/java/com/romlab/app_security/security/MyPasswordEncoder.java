package com.romlab.app_security.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/*@Component*/
public class MyPasswordEncoder /*implements PasswordEncoder*/{

    /*@Override*/
    public String encode(CharSequence rawPassword) {
        return String.valueOf(rawPassword.toString().hashCode());
    }

    /*@Override*/
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        var passwordAsString = String.valueOf(rawPassword.toString().hashCode());
        return encodedPassword.equals(passwordAsString);
    }
}
