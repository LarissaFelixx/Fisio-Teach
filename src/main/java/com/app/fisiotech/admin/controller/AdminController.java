package com.app.fisiotech.admin.controller;

import com.app.fisiotech.admin.service.AdminService;
import com.app.fisiotech.auth.dto.AlterarSenhaRequest;
import com.app.fisiotech.auth.security.AuthenticatedUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/me")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PutMapping("/senha")
    public ResponseEntity<Void> alterarPropriaSenha(
            @Valid @RequestBody AlterarSenhaRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        adminService.alterarSenha(usuarioLogado.getId(), request);
        return ResponseEntity.noContent().build();
    }

}
