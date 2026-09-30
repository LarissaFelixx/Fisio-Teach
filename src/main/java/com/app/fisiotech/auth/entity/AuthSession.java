package com.app.fisiotech.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "auth_sessions", indexes = @Index(name = "idx_auth_session_subject", columnList = "subject"))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class AuthSession {
    @Id @Column(length = 36) private String id;
    @Column(nullable = false, length = 80) private String subject;
    @Column(nullable = false, length = 64) private String credentialHash;
    @Column(nullable = false) private Instant expiresAt;
    @Column(nullable = false) private boolean revoked;

    public AuthSession(String id, String subject, String credentialHash, Instant expiresAt) {
        this.id = id;
        this.subject = subject;
        this.credentialHash = credentialHash;
        this.expiresAt = expiresAt;
    }
    public void revoke() { revoked = true; }
}
