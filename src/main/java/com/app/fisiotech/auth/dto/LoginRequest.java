package com.app.fisiotech.auth.dto;
import jakarta.validation.constraints.*;
public record LoginRequest(@NotBlank @Email @Size(max = 120) String email,
                           @NotBlank @Size(max = 200) String senha) {
    @Override public String toString() { return "LoginRequest[credentials=REDACTED]"; }
}
