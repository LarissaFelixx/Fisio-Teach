package com.app.fisiotech.auth.service;

import com.app.fisiotech.admin.repository.AdminRepository;
import com.app.fisiotech.auth.entity.PasswordResetToken;
import com.app.fisiotech.auth.repository.PasswordResetTokenRepository;
import com.app.fisiotech.auth.security.*;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PasswordRecoveryService {
    private final AppUserDetailsService users;
    private final PasswordResetTokenRepository tokens;
    private final RecoveryMailService mail;
    private final PasswordEncoder encoder;
    private final AccountSecurityService accountSecurity;
    private final AdminRepository admins;
    private final ProfissionalRepository profissionais;
    private final PacienteRepository pacientes;
    private final SecureRandom random = new SecureRandom();
    @Value("${app.password-recovery.ttl}") private Duration ttl;
    @Value("${app.password-recovery.max-requests-per-hour}") private long maxPerHour;
    @Value("${app.password-recovery.frontend-url}") private String frontendUrl;

    @Transactional
    public void request(String email) {
        AuthenticatedUser user;
        try { user = (AuthenticatedUser) users.loadUserByUsername(email); }
        catch (RuntimeException ignored) { return; }
        Instant now = Instant.now();
        if (tokens.countBySubjectAndCreatedAtAfter(user.subject(), now.minus(Duration.ofHours(1))) >= maxPerHour) return;
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokens.save(new PasswordResetToken(hash(raw), user.subject(), now.plus(ttl), now));
        mail.send(user.getEmail(), frontendUrl + "?token=" + URLEncoder.encode(raw, StandardCharsets.UTF_8));
    }

    @Transactional
    public void reset(String rawToken, String newPassword) {
        var token = tokens.findLocked(hash(rawToken)).orElseThrow(this::invalid);
        if (token.isConsumed() || !token.getExpiresAt().isAfter(Instant.now())) throw invalid();
        String[] identity = token.getSubject().split(":", 2);
        Long id;
        try { id = Long.valueOf(identity[1]); } catch (RuntimeException ex) { throw invalid(); }
        String hash = encoder.encode(newPassword);
        switch (identity[0]) {
            case "ADMIN" -> admins.findById(id).orElseThrow(this::invalid).setSenha(hash);
            case "PROFISSIONAL" -> profissionais.findById(id).orElseThrow(this::invalid).setSenha(hash);
            case "PACIENTE" -> pacientes.findById(id).orElseThrow(this::invalid).setSenha(hash);
            default -> throw invalid();
        }
        token.consume(); tokens.consumeAllBySubject(token.getSubject());
        accountSecurity.revoke(identity[0], id);
    }

    private BadCredentialsException invalid() { return new BadCredentialsException("Token inválido ou expirado."); }
    private String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}
