package com.carlos.challenge.infrastructure.in.web.dto.resp;


public record TokenResponse(String token, long expiresInSeconds) {}