package dev.oauth.authorizationserver.auth.dto;

import java.util.Set;

public record ClientRegisterRequest(
        String clientName,
        String redirectUri,
        Set<String> scopes
) {}
