package dev.oauth.authorizationserver.auth;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "oauth_clients")
public class OAuthClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String clientId;

    @Column(nullable = false)
    private String clientSecret;

    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private String redirectUri;

    @Column(nullable = false)
    private String scopes; // "openid, read, write"

    @Column(nullable = false)
    private String grantTypes; // "authorization_code, refresh_token"

    @Column(nullable = false)
    private String authMethods; // "client_secret_basic, client_secret_post"

    @Column(nullable = false)
    private boolean requireAuthorizationConsent;
}
