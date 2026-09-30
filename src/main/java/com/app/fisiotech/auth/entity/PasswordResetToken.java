package com.app.fisiotech.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens", indexes = @Index(name = "idx_password_reset_subject_created", columnList = "subject,created_at"))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PasswordResetToken {
    @Id @Column(length = 64) private String tokenHash;
    @Column(nullable = false, length = 80) private String subject;
    @Column(nullable = false) private Instant expiresAt;
    @Column(nullable = false) private Instant createdAt;
    @Column(nullable = false) private boolean consumed;

    public PasswordResetToken(String tokenHash, String subject, Instant expiresAt, Instant createdAt) {
        this.tokenHash = tokenHash; this.subject = subject; this.expiresAt = expiresAt; this.createdAt = createdAt;
    }
    public void consume() { consumed = true; }
}
