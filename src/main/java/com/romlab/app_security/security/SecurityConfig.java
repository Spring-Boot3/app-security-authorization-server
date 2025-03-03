package com.romlab.app_security.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /* Esta es la configuration por default de Spring Security
    * asi que si la eliminamos no pasará nada por lo mismo
    * de que es la configuration por default se va a estar ejecutando */
    /*@Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(auth ->
                *//* Con esta instruccion colocamos que cualquier peticion
                * tiene que venir de un cliente autenticado para poder acceder
                * a nuestros servicios *//*
                auth.anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());
        return httpSecurity.build();
    }*/

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(auth ->
                auth.requestMatchers("/loans", "/account", "/balance", "/cards").authenticated()
//                .requestMatchers("/welcome", "/about-us").permitAll())
                .anyRequest().permitAll()) //Esto es lo mismo que lo de arriba solo que global en todo lo demas
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());
        return httpSecurity.build();
    }
}
