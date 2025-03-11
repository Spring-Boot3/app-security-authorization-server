package com.romlab.app_security.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.romlab.app_security.services.CustomerUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

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
    private static final String APPLICATION_OWNER = "RomLab Agency";

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
    /**
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

    // Configuracion del Authorization Server
    @Bean
    @Order(1)
    SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults());
        http.exceptionHandling(e -> e.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(LOGIN_RESOURCE)));
        return http.build();
    }

    // Configuracion para el cliente
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

    // Configuracion para el usuario
/*    @Bean
    @Order(3)
    SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth2 -> auth2
                .requestMatchers(ADMIN_RESOURCES).hasRole(ROLE_ADMIN)
                .requestMatchers(USER_RESOURCES).hasRole(ROLE_USER)
                .anyRequest().permitAll());
        return http.build();
    }*/

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Provee la autenticacion
    @Bean
    AuthenticationProvider authenticationProvider(PasswordEncoder encoder, CustomerUserDetails userDetails) {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(encoder);
        authProvider.setUserDetailsService(userDetails);
        return authProvider;
    }

    // Configura el authorizationServer de autorizacion
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    // Configuracion de JWT en roles
    @Bean
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter() {
        var converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix("");
        return converter;
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(JwtGrantedAuthoritiesConverter settings) {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(settings);
        return converter;
    }

    // JWK => Json Web Key
    /* Aqui es Json Web Key es con lo que vamos a firmar el token */
    @Bean
    JWKSource<SecurityContext> jwkSource(){
        RSAKey rsa = generateKeys();
        var jwkSet = new JWKSet(rsa);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    // Decodifica el jwk generado por las lla publica y privada
    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    // Integrar los claims en el JWT
    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> oAuth2TokenCustomizer(){
        return context -> {
            if(context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)){
               context.getClaims().claims(claim -> claim.putAll(Map.of("owner", APPLICATION_OWNER, "date_request", LocalDate.now().toString())));
            }
        };
    }

    /* Este es nuestro algoritmo para generar el RSA */
    private static KeyPair generateRSA() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
            keyPairGenerator.initialize(RSA_SIZE);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e){
            throw new IllegalStateException(e);
        }
        return keyPair;
    }

    private static RSAKey generateKeys() {
        var keyPair = generateRSA();
        var publicKey = (RSAPublicKey) keyPair.getPublic();
        var privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
    }

}
