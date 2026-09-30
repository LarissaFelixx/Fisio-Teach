package com.app.fisiotech.auth.service;

public interface RecoveryMailService {
    void send(String email, String resetUrl);
}
