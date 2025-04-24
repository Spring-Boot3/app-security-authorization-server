package com.romlab.app_security.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.romlab.app_security.components.CustomAuthenticationFailureHandler;
import com.romlab.app_security.components.HtmxLoginSuccessHandler;
import com.romlab.app_security.services.CustomerUserDetails;
import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private static final String LOGIN_RESOURCE = "/login";
    private static final String RSA = "RSA";
    private static final Integer RSA_SIZE = 2048;
    private static final String APPLICATION_OWNER = "SEI";

    private HtmxLoginSuccessHandler htmxLoginSuccessHandler;
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    // Configuracion del Authorization Server
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        http.setSharedObject(OAuth2TokenGenerator.class, tokenGenerator());
        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (authorizationServer) ->
                        authorizationServer.oidc(Customizer.withDefaults())
                ).authorizeHttpRequests((authorize) ->
                        authorize.anyRequest().authenticated()
                ).cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(e -> e.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint(LOGIN_RESOURCE)));
        return http.build();
    }

    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator() {
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource());
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

    @Bean
    public OAuth2TokenCustomizer<OAuth2TokenContext> refreshTokenCustomizer() {
        return context -> {
            if (OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
                System.out.println("✅ Generando refresh token para: " + context.getPrincipal().getName());
            }
        };
    }


    @Bean
    SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/login", "/oidc/logout").permitAll()
                .anyRequest().permitAll()
        ).formLogin(formLogin ->
                formLogin
                        .loginPage(LOGIN_RESOURCE)
                        .loginProcessingUrl(LOGIN_RESOURCE)
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(htmxLoginSuccessHandler)
                        .failureHandler(customAuthenticationFailureHandler)
                        .permitAll()
                )
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200/", "https://oauthdebugger.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Origin", "Accept", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

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
        return AuthorizationServerSettings.builder()
                .oidcLogoutEndpoint("/oauth2/logout")
                .build();
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
            var authentication = context.getPrincipal();
            Set<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            if(context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)){
                Map<String, Object> claims = new HashMap<>();
                claims.put("roles", authorities);
                claims.put("owner", APPLICATION_OWNER);
                claims.put("date_request", LocalDateTime.now().toString());
                try {
                    UserDetails user = (UserDetails) authentication.getPrincipal();
                    /*AuthUserRoleDto authUserRoleDto = usuarioConnection.loadUserByUsername(UsuarioAuthDto.builder().username(user.getUsername()).build());
                    claims.put("uid", authUserRoleDto.getId());*/
                } catch (Exception e) {
                    System.out.println("Error de usuario");
                }
                context.getClaims().claims(claim -> claim.putAll(claims));
            }
            if (context.getTokenType().equals(OAuth2TokenType.REFRESH_TOKEN)) {
                System.out.println("⚠️ Generando refresh token para: " + context.getPrincipal().getName());
                context.getClaims().claims(claim -> {
                    claim.put("refresh_token_info", "Issued by SEI");
                });
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

    /*Genera las llaves publicas y privadas*/
    private static RSAKey generateKeys() {
        var keyPair = generateRSA();
        var publicKey = (RSAPublicKey) keyPair.getPublic();
        var privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
    }

}
