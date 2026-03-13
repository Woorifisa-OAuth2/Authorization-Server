package dev.oauth.authorizationserver.auth.dto;

public record ClientRegisterResponse(
        String clientId,
        String clientSecret
) {}
