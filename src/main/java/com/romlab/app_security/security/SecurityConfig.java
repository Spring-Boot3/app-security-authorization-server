package com.romlab.app_security.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

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
                auth
                .requestMatchers("/loans", "/account", "/balance", "/cards").authenticated()
//                .requestMatchers("/welcome", "/about-us").permitAll())
                .anyRequest().permitAll()) //Esto es lo mismo que lo de arriba solo que global en todo lo demas
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());
        /*Esto es para deshabilitar los CORS y CSRF !No recomendado para ambientes deployados!*/
        httpSecurity.cors(AbstractHttpConfigurer::disable);
        httpSecurity.csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }

    /*@Bean
    InMemoryUserDetailsManager inMemoryUserDetailsManager() throws Exception {
        UserDetails admin = User
                .withUsername("admin")
                .password("to-be-encoded")
                .authorities("ADMIN")
                .build();

        var user = User
                .withUsername("user")
                .password("to-be-encoded")
                .authorities("USER")
                .build();
        return new InMemoryUserDetailsManager(admin, user);
    }*/

    /*@Bean
    UserDetailsService userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }*/

    /* Esto solo nos ayudara a mitigar el error de no enviar encoded asi que solo usarlo en pruebas o desarrollo */
/*    @Bean
    PasswordEncoder passwordEncoder() throws Exception {
        return NoOpPasswordEncoder.getInstance();
    }*/

    @Bean
    PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
