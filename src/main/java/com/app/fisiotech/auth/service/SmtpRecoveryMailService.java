package com.app.fisiotech.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
class SmtpRecoveryMailService implements RecoveryMailService {
    private final JavaMailSender sender;
    @Value("${app.mail.from}") private String from;
    public void send(String email, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from); message.setTo(email); message.setSubject("Recuperação de senha FisioTech");
        message.setText("Use o link para redefinir sua senha. Ele é temporário e de uso único:\n" + resetUrl);
        sender.send(message);
    }
}
