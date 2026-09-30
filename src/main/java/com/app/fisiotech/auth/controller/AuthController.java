package com.app.fisiotech.auth.controller;

import com.app.fisiotech.auth.dto.MeResponse;
import com.app.fisiotech.auth.security.AuthenticatedUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal AuthenticatedUser usuarioLogado) {
        return ResponseEntity.ok(MeResponse.fromAuthenticatedUser(usuarioLogado));
    }

}
