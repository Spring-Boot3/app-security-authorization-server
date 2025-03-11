package com.romlab.app_security.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
public class SecurityConfig {

    private static final String[] USER_RESOURCES = {"/loans/**","/balance/**"};
    private static final String[] ADMIN_RESOURCES = {"/accounts/**","/cards/**"};
    private static final String AUTH_WRITE = "write";
    private static final String AUTH_READ = "read";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final String LOGIN_RESOURCE = "/login";
    private static final String RSA = "RSA";
    private static final Integer RSA_SIZE = 2048;
    private static final String APPLICATION_OWNER = "Debuggeando ideas";

    /**
     *  ----------------------------------------------------------------------
     * | Esta configuracion es para nuestro cliente osea una app web o movile |
     *  ----------------------------------------------------------------------
     * Esta es una configuracion estatica que no es recomendable ya que
     * si se agrega un nuevo cliente se tiene que matar el servicio y volver
     * a cargar con la nueva configuracion, es por eso que se recomienda
     * hacer esto desde la BD para que en tiempo de ejecucion podamos
     * agregar todos los clientes que queramos.
     * */
    /**@Bean
    RegisteredClientRepository clientRepository() {
        var client = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("romLab")
                .clientSecret("secret")
                .scope("read")
                .redirectUri("http://localhost:8080")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .build();
        return new InMemoryRegisteredClientRepository(client);
    }*/

    @Bean
    @Order(1)
    SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());
        http.exceptionHandling(e -> e.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(LOGIN_RESOURCE)));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain clientSecurityFilterChain(HttpSecurity http) throws Exception {
        http.formLogin(Customizer.withDefaults());
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(ADMIN_RESOURCES).hasAuthority(AUTH_WRITE)
                .requestMatchers(USER_RESOURCES).hasAuthority(AUTH_READ)
                .anyRequest().permitAll());
        http.oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));

        return http.build();
    }

}
