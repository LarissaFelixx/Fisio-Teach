package com.app.fisiotech.auth.controller;

import com.app.fisiotech.auth.dto.MeResponse;
import com.app.fisiotech.auth.dto.RecuperarSenhaRequest;
import com.app.fisiotech.auth.dto.RedefinirSenhaRequest;
import com.app.fisiotech.auth.security.AuthenticatedUser;
import com.app.fisiotech.auth.service.RecuperacaoSenhaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RecuperacaoSenhaService recuperacaoSenhaService;

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal AuthenticatedUser usuarioLogado) {
        return ResponseEntity.ok(MeResponse.fromAuthenticatedUser(usuarioLogado));
    }

    // Sempre 204, exista ou não o email - ver RecuperacaoSenhaService.solicitarCodigo.
    @PostMapping("/recuperar-senha")
    public ResponseEntity<Void> recuperarSenha(@RequestBody @Valid RecuperarSenhaRequest request) {
        recuperacaoSenhaService.solicitarCodigo(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@RequestBody @Valid RedefinirSenhaRequest request) {
        recuperacaoSenhaService.redefinirSenha(request);
        return ResponseEntity.noContent().build();
    }

}
