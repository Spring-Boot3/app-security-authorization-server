package com.romlab.app_security.security;

import com.romlab.app_security.security.filters.JWTValidationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.sql.DataSource;
import java.util.List;

@Configuration
// Esta anotacion es necesaria para poder utilizar el @PreAuthorize a nivel controller o capa de servicio
//@EnableMethodSecurity
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
    @Autowired
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JWTValidationFilter jwtValidationFilter) throws Exception {
        //httpSecurity.addFilterBefore(new ApiKeyFilter(), BasicAuthenticationFilter.class);
        httpSecurity.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        var requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName("_csrf");
        httpSecurity.authorizeHttpRequests(auth ->
                auth
//                .requestMatchers("/loans", "/account", "/balance", "/cards").authenticated()
//                .requestMatchers("/welcome", "/about-us").permitAll())
                .requestMatchers("/loans").hasRole("USER")
                .requestMatchers("/balance").hasRole("USER")
                .requestMatchers("/cards").hasRole("ADMIN")
                .requestMatchers("/account").hasRole("ADMIM")
                .anyRequest().permitAll()) //Esto es lo mismo que lo de arriba solo que global en todo lo demas
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());
        /*Esto es para deshabilitar los CORS y CSRF !No recomendado para ambientes deployados!*/
       /* httpSecurity.cors(AbstractHttpConfigurer::disable);
        httpSecurity.csrf(AbstractHttpConfigurer::disable);*/
        httpSecurity.addFilterAfter(jwtValidationFilter, BasicAuthenticationFilter.class);
        httpSecurity.cors(cors -> corsConfigurationSource());
        httpSecurity.csrf(csrf -> csrf.csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers("/welcome", "/about-us", "/api/auth")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
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

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        /* Aqui estamos dando la instruccion de que origenes si sera aceptada la peticion */
        //config.setAllowedOrigins(List.of("http://localhost:4200/"));
        config.setAllowedOrigins(List.of("*")); // Con esta instruccion la app respondera desde cualquier origen

        /* Aqui estamos dando la instruccion de que metodos podran ejecutarce en nuestra app */
        //config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedMethods(List.of("*")); // Con esta instruccion aceptara todos los metodos HTTP

        config.setAllowedHeaders(List.of("*")); // Con esta instruccion aceptara todos los headers

        var source = new UrlBasedCorsConfigurationSource();
        /* Aqui registramos toda la configuracion sobre los CORS
        * el "/**" quiere decir que la configuracion sera aplicada a todos mis recursos(endpoints) */
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
