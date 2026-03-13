package dev.oauth.authorizationserver.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.stereotype.Component;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JpaRegisteredClientRepository implements RegisteredClientRepository {

    private final OAuthClientRepository oauthClientRepository;

    @Override
    public void save(RegisteredClient registeredClient) {
        OAuthClient entity = OAuthClient.builder()
                .clientId(registeredClient.getClientId())
                .clientSecret(registeredClient.getClientSecret())
                .clientName(registeredClient.getClientName())
                .redirectUri(registeredClient.getRedirectUris().iterator().next())
                .scopes(String.join(",", registeredClient.getScopes()))
                .grantTypes(registeredClient.getAuthorizationGrantTypes().stream()
                        .map(AuthorizationGrantType::getValue)
                        .collect(Collectors.joining(",")))
                .authMethods(registeredClient.getClientAuthenticationMethods().stream()
                        .map(ClientAuthenticationMethod::getValue)
                        .collect(Collectors.joining(",")))
                .requireAuthorizationConsent(
                        registeredClient.getClientSettings().isRequireAuthorizationConsent()
                )
                .build();

        oauthClientRepository.save(entity);
    }

    @Override
    public RegisteredClient findById(String id) {
        return null;
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return oauthClientRepository.findByClientId(clientId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    private RegisteredClient toRegisteredClient(OAuthClient entity) {
        RegisteredClient.Builder builder = RegisteredClient.withId(String.valueOf(entity.getId()))
                .clientId(entity.getClientId())
                .clientSecret(entity.getClientSecret())
                .clientName(entity.getClientName())
                .redirectUri(entity.getRedirectUri())
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(entity.isRequireAuthorizationConsent())
                        .build());

        Arrays.stream(entity.getScopes().split(","))
                .forEach(builder::scope);

        Arrays.stream(entity.getGrantTypes().split(","))
                .map(AuthorizationGrantType::new)
                .forEach(builder::authorizationGrantType);

        Arrays.stream(entity.getAuthMethods().split(","))
                .map(ClientAuthenticationMethod::new)
                .forEach(builder::clientAuthenticationMethod);

        return builder.build();
    }
}
