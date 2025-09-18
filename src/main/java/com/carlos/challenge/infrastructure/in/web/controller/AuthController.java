package com.carlos.challenge.infrastructure.in.web.controller;

import com.carlos.challenge.infrastructure.config.JwtProperties;
import com.carlos.challenge.infrastructure.config.JwtTokenService;
import com.carlos.challenge.infrastructure.in.web.dto.req.LoginRequest;
import com.carlos.challenge.infrastructure.in.web.dto.resp.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtTokenService tokenService;
    private final JwtProperties props;

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> token(@RequestBody LoginRequest req) {
        try {
            var auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
            String jwt = tokenService.generateToken(auth);
            return ResponseEntity.ok(new TokenResponse(jwt, 3600));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).build();
        }
    }

}
