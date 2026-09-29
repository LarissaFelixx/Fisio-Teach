package com.app.fisiotech.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth_refresh_tokens", indexes = @Index(name = "idx_refresh_session", columnList = "session_id"))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class RefreshToken {
    @Id @Column(length = 64) private String tokenHash;
    @Column(name = "session_id", nullable = false, length = 36) private String sessionId;
    @Column(nullable = false) private boolean consumed;

    public RefreshToken(String tokenHash, String sessionId) {
        this.tokenHash = tokenHash;
        this.sessionId = sessionId;
    }
    public void consume() { consumed = true; }
}
