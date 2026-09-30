package com.app.fisiotech.auth.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthenticatedUser implements UserDetails {

    public String role() { return authorities.iterator().next().getAuthority(); }
    public String subject() { return role().substring("ROLE_".length()) + ":" + id; }

    private final Long id;
    private final String nome;
    private final String email;
    private final String senha;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedUser(Long id, String nome, String email, String senha, String role) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.authorities = List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
