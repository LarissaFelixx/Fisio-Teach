package com.app.fisiotech.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Size(max = 200) String token,
        @NotBlank @Size(min = 8, max = 100) String novaSenha) {
    @Override public String toString() { return "ResetPasswordRequest[credentials=REDACTED]"; }
}
