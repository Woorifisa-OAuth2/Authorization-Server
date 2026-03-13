package dev.oauth.authorizationserver.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class ReactLoginEntryPoint implements AuthenticationEntryPoint {

    private static final String REACT_LOGIN_URL = "http://localhost:3000/login";

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String fullRequestUrl = request.getRequestURL().toString();
        if (request.getQueryString() != null) {
            fullRequestUrl += "?" + request.getQueryString();
        }

        String redirectUrl = UriComponentsBuilder
                .fromUriString(REACT_LOGIN_URL)
                .queryParam("returnUrl", URLEncoder.encode(fullRequestUrl, StandardCharsets.UTF_8))
                .build(true)
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
