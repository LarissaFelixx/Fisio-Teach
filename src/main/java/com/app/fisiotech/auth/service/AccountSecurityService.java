package com.app.fisiotech.auth.service;

import com.app.fisiotech.auth.entity.AuthEmail;
import com.app.fisiotech.auth.repository.*;
import com.app.fisiotech.exception.EmailJaCadastradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AccountSecurityService {
    private final AuthEmailRepository emails;
    private final AuthSessionRepository sessions;

    @Transactional
    public void registerEmail(String type, Long id, String email) {
        String subject = type + ":" + id;
        AuthEmail entry = emails.findById(subject).orElseGet(() -> new AuthEmail(subject, email));
        entry.changeEmail(email.trim().toLowerCase(Locale.ROOT));
        try {
            emails.saveAndFlush(entry);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailJaCadastradoException("Já existe uma conta com este email.");
        }
    }

    @Transactional
    public void revoke(String type, Long id) { sessions.revokeBySubject(type + ":" + id); }

    @Transactional
    public void deleteAccount(String type, Long id) {
        revoke(type, id);
        emails.deleteById(type + ":" + id);
    }
}
