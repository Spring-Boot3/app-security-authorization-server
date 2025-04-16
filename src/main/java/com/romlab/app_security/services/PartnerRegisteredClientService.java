package com.romlab.app_security.services;

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

@Service
@AllArgsConstructor
public class PartnerRegisteredClientService implements RegisteredClientRepository {

    private PartnerRepository partnerRepository;

    @Override
    public RegisteredClient findByClientId(String clientId) {
        var partnerOpt = partnerRepository.findByClientId(clientId);
        System.out.println("--->>>> " + partnerOpt);
        return partnerOpt.map(partner -> {
            var authorizationGrantType = Arrays.stream(partner.getGrantTypes().split(","))
                    .map(AuthorizationGrantType::new)
                    .toList();

            var clientAuthenticationMethods = Arrays.stream(partner.getAuthenticationMethods().split(","))
                    .map(ClientAuthenticationMethod::new)
                    .toList();

            var scopes = Arrays.stream(partner.getScopes().split(",")).toList();

            return RegisteredClient
                    .withId(partner.getId().toString())
                    .clientId(partner.getClientId())
                    .clientSecret(partner.getClientSecret())
                    .clientName(partner.getClientName())
                    .redirectUri(partner.getRedirectUri())
                    .postLogoutRedirectUri(partner.getRedirectUriLogout())
                    .clientAuthenticationMethod(clientAuthenticationMethods.get(0))
                    .clientAuthenticationMethod(clientAuthenticationMethods.get(1))
                    .authorizationGrantType(authorizationGrantType.get(0))
                    .authorizationGrantType(authorizationGrantType.get(1))
                    .scope(scopes.get(0))
                    .scope(scopes.get(1))
                    .scope(scopes.get(2))
                    .tokenSettings(tokenSettings())
                    .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                    .build();
        }).orElseThrow(() -> new BadCredentialsException("Client not exist!!!!"));
    }

    @Override
    public void save(RegisteredClient registeredClient) {

    }

    @Override
    public RegisteredClient findById(String id) {
        return null;
    }

    private TokenSettings tokenSettings() {

        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(8))
                .build();
    }

}
