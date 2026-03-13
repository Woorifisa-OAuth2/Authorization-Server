package dev.oauth.authorizationserver.config;

import dev.oauth.authorizationserver.user.UserAccount;
import dev.oauth.authorizationserver.user.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
@RequiredArgsConstructor
public class TokenCustomizerConfig {

    private final UserRepository userRepository;

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return context -> {

            Authentication principal = context.getPrincipal();

            if (principal == null) {
                return;
            }

            String username = principal.getName();

            UserAccount user = userRepository.findByUsername(username)
                    .orElseThrow(() ->
                            new IllegalArgumentException("사용자를 찾을 수 없습니다. username=" + username)
                    );

            // Access Token 커스터마이징
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {

                context.getClaims().claim("user_id", user.getId());
                context.getClaims().claim("username", user.getUsername());
                context.getClaims().claim("role", user.getRole());
                context.getClaims().claim("name", user.getName());
                context.getClaims().claim("age", user.getAge());
                context.getClaims().claim("gender", user.getGender());
                context.getClaims().claim("email", user.getEmail());
            }

            // ID Token 커스터마이징
            if ("id_token".equals(context.getTokenType().getValue())) {

                context.getClaims().claim("user_id", user.getId());
                context.getClaims().claim("username", user.getUsername());
            }
        };
    }
}