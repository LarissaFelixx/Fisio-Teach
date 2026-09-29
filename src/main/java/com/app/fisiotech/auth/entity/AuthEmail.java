package com.app.fisiotech.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auth_emails", uniqueConstraints = @UniqueConstraint(name = "uk_auth_email", columnNames = "email"))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class AuthEmail {
    @Id @Column(length = 80) private String subject;
    @Column(nullable = false, length = 120) private String email;
    public AuthEmail(String subject, String email) { this.subject = subject; this.email = email; }
    public void changeEmail(String email) { this.email = email; }
}
