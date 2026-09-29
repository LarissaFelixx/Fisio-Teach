package com.app.fisiotech.auth.service;

import com.app.fisiotech.auth.dto.*;
import com.app.fisiotech.auth.entity.*;
import com.app.fisiotech.auth.repository.*;
import com.app.fisiotech.auth.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final AppUserDetailsService users;
    private final AuthSessionRepository sessions;
    private final RefreshTokenRepository refreshTokens;
    private final JwtEncoder encoder;
    private final Clock clock;
    private final EntityManager entityManager;
    private final String issuer, audience;
    private final Duration accessTtl, refreshTtl;
    private final SecureRandom random = new SecureRandom();

    public AuthService(AuthenticationManager authenticationManager, AppUserDetailsService users,
            AuthSessionRepository sessions, RefreshTokenRepository refreshTokens, JwtEncoder encoder,
            Clock clock, EntityManager entityManager,
            @Value("${app.jwt.issuer}") String issuer, @Value("${app.jwt.audience}") String audience,
            @Value("${app.jwt.access-ttl}") Duration accessTtl, @Value("${app.jwt.refresh-ttl}") Duration refreshTtl) {
        if (accessTtl.isNegative() || accessTtl.getSeconds() < 1 || refreshTtl.compareTo(accessTtl) < 0) {
            throw new IllegalArgumentException("Validades dos tokens inválidas.");
        }
        this.authenticationManager = authenticationManager; this.users = users;
        this.sessions = sessions; this.refreshTokens = refreshTokens; this.encoder = encoder;
        this.clock = clock; this.entityManager = entityManager; this.issuer = issuer; this.audience = audience;
        this.accessTtl = accessTtl; this.refreshTtl = refreshTtl;
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.senha()));
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        AuthSession session = new AuthSession(UUID.randomUUID().toString(), user.subject(),
                hash(user.getPassword()), clock.instant().plus(refreshTtl));
        sessions.save(session);
        return issue(session, user);
    }

    // Replay must commit revocation even though the HTTP result is 401.
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public TokenResponse refresh(String rawToken) {
        RefreshToken token = refreshTokens.findById(hash(rawToken)).orElseThrow(this::invalid);
        AuthSession session = sessions.findLockedById(token.getSessionId()).orElseThrow(this::invalid);
        // Reload after obtaining the session lock: another transaction may have rotated this token.
        entityManager.refresh(token, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        if (token.isConsumed()) {
            session.revoke();
            throw invalid();
        }
        AuthenticatedUser user = validateSession(session);
        token.consume();
        return issue(session, user);
    }

    @Transactional
    public void logout(String rawToken) {
        refreshTokens.findById(hash(rawToken)).ifPresent(token ->
                sessions.findLockedById(token.getSessionId()).ifPresent(AuthSession::revoke));
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser authenticate(Jwt jwt) {
        AuthSession session = sessions.findById(jwt.getClaimAsString("sid")).orElseThrow(this::invalid);
        if (!session.getSubject().equals(jwt.getSubject())) throw invalid();
        AuthenticatedUser user = validateSession(session);
        if (!user.role().equals(jwt.getClaimAsString("role"))) throw invalid();
        return new AuthenticatedUser(user.getId(), user.getNome(), user.getEmail(), null, user.role());
    }

    private AuthenticatedUser validateSession(AuthSession session) {
        if (session.isRevoked() || !session.getExpiresAt().isAfter(clock.instant())) throw invalid();
        AuthenticatedUser user = users.loadBySubject(session.getSubject());
        // Also closes the race between a password change and an in-flight login/refresh.
        if (!MessageDigest.isEqual(session.getCredentialHash().getBytes(StandardCharsets.UTF_8),
                hash(user.getPassword()).getBytes(StandardCharsets.UTF_8))) throw invalid();
        return user;
    }

    private TokenResponse issue(AuthSession session, AuthenticatedUser user) {
        Instant now = clock.instant();
        Instant expires = now.plus(accessTtl);
        if (expires.isAfter(session.getExpiresAt())) expires = session.getExpiresAt();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).audience(List.of(audience))
                .subject(session.getSubject()).issuedAt(now).expiresAt(expires)
                .id(UUID.randomUUID().toString()).claim("sid", session.getId()).claim("role", user.role()).build();
        String access = encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(), claims)).getTokenValue();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String refresh = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshTokens.save(new RefreshToken(hash(refresh), session.getId()));
        return new TokenResponse(access, "Bearer", Duration.between(now, expires).getSeconds(), refresh,
                Duration.between(now, session.getExpiresAt()).getSeconds());
    }

    private BadCredentialsException invalid() { return new BadCredentialsException("Sessão inválida ou expirada."); }

    private static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }
}
