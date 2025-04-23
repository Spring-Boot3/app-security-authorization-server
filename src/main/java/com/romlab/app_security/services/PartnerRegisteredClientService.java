package com.romlab.app_security.services;

import com.romlab.app_security.entities.PartnerEntity;
import com.romlab.app_security.repositories.PartnerRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class PartnerRegisteredClientService implements RegisteredClientRepository {

    private PartnerRepository partnerRepository;

    @Override
    public void save(RegisteredClient registeredClient) {}

    @Override
    public RegisteredClient findById(String id) {
        return null;
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {

        Optional<PartnerEntity> partnerOpt = partnerRepository.findByClientId(clientId);

        return partnerOpt.map(partner -> {

            List<AuthorizationGrantType> authorizationGrantTypes = Arrays.stream(partner.getGrantTypes().split(","))
                    .map(String::trim)
                    .map(grantType -> {
                        System.out.println("Grant type: " + grantType);
                        if ("refresh_token".equalsIgnoreCase(grantType)) {
                            return AuthorizationGrantType.REFRESH_TOKEN;
                        }
                        if ("authorization_code".equalsIgnoreCase(grantType)) {
                            return AuthorizationGrantType.AUTHORIZATION_CODE;
                        }
                        if ("client_credentials".equalsIgnoreCase(grantType)) {
                            return AuthorizationGrantType.CLIENT_CREDENTIALS;
                        }
                        return new AuthorizationGrantType(grantType);
                    }).toList();

            List<ClientAuthenticationMethod> clientAuthenticationMethods = Arrays.stream(partner.getAuthenticationMethods().split(","))
                    .map(String::trim)
                    .map(method -> {
                        if ("none".equalsIgnoreCase(method)){
                            return ClientAuthenticationMethod.NONE;
                        }
                        return new ClientAuthenticationMethod(method);
                    }).toList();

            List<String> scopes = Arrays.stream(partner.getScopes().split(",")).toList();

            ClientSettings.Builder clientSettingsBuilder = ClientSettings.builder()
                    .requireAuthorizationConsent(false);

            boolean isPublicClient = clientAuthenticationMethods.contains(ClientAuthenticationMethod.NONE) &&
                    clientAuthenticationMethods.stream().noneMatch(m ->
                            m.equals(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) ||
                                    m.equals(ClientAuthenticationMethod.CLIENT_SECRET_POST) ||
                                    m.equals(ClientAuthenticationMethod.CLIENT_SECRET_JWT));

            boolean usesAuthorizationCode = authorizationGrantTypes.contains(AuthorizationGrantType.AUTHORIZATION_CODE);

            if (isPublicClient && usesAuthorizationCode) {
                clientSettingsBuilder.requireProofKey(true);
            } else {
                System.out.println("PKCE *not* required for client: " + clientId + " (isPublic: " + isPublicClient + ", usesCode: " + usesAuthorizationCode +")");
            }

            RegisteredClient.Builder registeredClientBuilder = RegisteredClient.withId(partner.getId().toString()).clientId(partner.getClientId());

            if (partner.getClientSecret() != null && !partner.getClientSecret().isEmpty()) {
                registeredClientBuilder.clientSecret(partner.getClientSecret());
            }

            registeredClientBuilder
                    .clientName(partner.getClientName())
                    .redirectUris(uris -> uris.addAll(Set.of(partner.getRedirectUri().split(","))))
                    .postLogoutRedirectUris(uris -> uris.addAll(Set.of(partner.getRedirectUriLogout().split(","))))
                    .tokenSettings(tokenSettings())
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .clientSettings(
                            clientSettingsBuilder
                                    .requireAuthorizationConsent(false)
                                    .setting("settings.allow_public_client_refresh_token", true)
                                    .build()
                    );
            clientAuthenticationMethods.forEach(registeredClientBuilder::clientAuthenticationMethod);
            scopes.forEach(registeredClientBuilder::scope);
            return registeredClientBuilder.build();
        }).orElseThrow(() -> new BadCredentialsException("Client '" + clientId + "' not found in PartnerRepository!"));
    }

    private TokenSettings tokenSettings() {
        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(8))
                .refreshTokenTimeToLive(Duration.ofDays(30))
                .reuseRefreshTokens(false)
                .build();
    }

}
