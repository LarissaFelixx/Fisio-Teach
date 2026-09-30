package com.app.fisiotech.auth.dto;
import jakarta.validation.constraints.*;
public record RefreshTokenRequest(@NotBlank @Size(max = 200) String refreshToken) {
    @Override public String toString() { return "RefreshTokenRequest[token=REDACTED]"; }
}
