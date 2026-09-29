package com.app.fisiotech.auth.dto;
public record TokenResponse(String accessToken, String tokenType, long expiresIn,
                            String refreshToken, long refreshExpiresIn) {
    @Override public String toString() { return "TokenResponse[tokens=REDACTED]"; }
}
