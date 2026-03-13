package dev.oauth.authorizationserver.controller;

import dev.oauth.authorizationserver.auth.dto.ClientRegisterRequest;
import dev.oauth.authorizationserver.auth.dto.ClientRegisterResponse;
import dev.oauth.authorizationserver.auth.service.ClientRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clients")
public class ClientRegistrationController {

    private final ClientRegistrationService clientRegistrationService;

    @PostMapping
    public ResponseEntity<ClientRegisterResponse> register(@RequestBody ClientRegisterRequest request) {
        return ResponseEntity.ok(clientRegistrationService.register(request));
    }
}