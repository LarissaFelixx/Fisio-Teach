package com.app.fisiotech.auth.service;

import com.app.fisiotech.admin.entity.Admin;
import com.app.fisiotech.admin.repository.AdminRepository;
import com.app.fisiotech.auth.entity.PasswordResetToken;
import com.app.fisiotech.auth.repository.PasswordResetTokenRepository;
import com.app.fisiotech.auth.security.*;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {
    @Mock AppUserDetailsService users;
    @Mock PasswordResetTokenRepository tokens;
    @Mock RecoveryMailService mail;
    @Mock PasswordEncoder encoder;
    @Mock AccountSecurityService accountSecurity;
    @Mock AdminRepository admins;
    @Mock ProfissionalRepository profissionais;
    @Mock PacienteRepository pacientes;
    @InjectMocks PasswordRecoveryService service;

    @BeforeEach void configure() {
        ReflectionTestUtils.setField(service, "ttl", Duration.ofMinutes(15));
        ReflectionTestUtils.setField(service, "maxPerHour", 3L);
        ReflectionTestUtils.setField(service, "frontendUrl", "https://app.example/reset");
    }

    @Test void mantemRespostaNeutraParaEmailInexistente() {
        when(users.loadUserByUsername(anyString())).thenThrow(new UsernameNotFoundException("absent"));
        service.request("absent@example.com");
        verifyNoInteractions(tokens, mail);
    }

    @Test void limitaSolicitacoesSemEnviarNovoEmail() {
        var user = new AuthenticatedUser(1L, "Admin", "admin@example.com", "hash", "ROLE_ADMIN");
        when(users.loadUserByUsername(anyString())).thenReturn(user);
        when(tokens.countBySubjectAndCreatedAtAfter(eq("ADMIN:1"), any())).thenReturn(3L);
        service.request(user.getEmail());
        verify(tokens, never()).save(any()); verifyNoInteractions(mail);
    }

    @Test void consomeTokenAlteraSenhaERevogaSessoes() {
        var token = new PasswordResetToken("hash", "ADMIN:1", Instant.now().plusSeconds(60), Instant.now());
        var admin = new Admin("Admin", "admin@example.com", "old");
        when(tokens.findLocked(anyString())).thenReturn(Optional.of(token));
        when(admins.findById(1L)).thenReturn(Optional.of(admin));
        when(encoder.encode("novaSenha123")).thenReturn("new-hash");

        service.reset("raw-token", "novaSenha123");

        assertThat(admin.getSenha()).isEqualTo("new-hash");
        assertThat(token.isConsumed()).isTrue();
        verify(tokens).consumeAllBySubject("ADMIN:1");
        verify(accountSecurity).revoke("ADMIN", 1L);
    }
}
