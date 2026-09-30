package com.app.fisiotech.auth.controller;

import com.app.fisiotech.auth.dto.*;
import com.app.fisiotech.auth.security.AuthenticatedUser;
import com.app.fisiotech.auth.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final com.app.fisiotech.auth.service.PasswordRecoveryService passwordRecoveryService;

    @PostMapping("/forgot-password")
    @SecurityRequirements
    public ResponseEntity<java.util.Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordRecoveryService.request(request.email());
        return ResponseEntity.accepted().body(java.util.Map.of("message",
                "Se o email estiver cadastrado, as instruções serão enviadas."));
    }

    @PostMapping("/reset-password")
    @SecurityRequirements
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordRecoveryService.reset(request.token(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    @SecurityRequirements
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(authService.login(request));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    @SecurityRequirements
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal AuthenticatedUser usuarioLogado) {
        return ResponseEntity.ok(MeResponse.fromAuthenticatedUser(usuarioLogado));
    }
}
