package com.app.fisiotech.auth.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
class DisabledRecoveryMailService implements RecoveryMailService {
    public void send(String email, String resetUrl) {
        // Desenvolvimento sem SMTP: mantém o contrato sem expor o token em logs.
    }
}
