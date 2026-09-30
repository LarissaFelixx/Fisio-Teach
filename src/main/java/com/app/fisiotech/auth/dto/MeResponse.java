package com.app.fisiotech.auth.dto;

import com.app.fisiotech.auth.security.AuthenticatedUser;

public record MeResponse(
        Long id,
        String nome,
        String email,
        String role
) {

    public static MeResponse fromAuthenticatedUser(AuthenticatedUser usuarioLogado) {
        String role = usuarioLogado.getAuthorities().iterator().next().getAuthority();
        return new MeResponse(usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getEmail(), role);
    }
}
