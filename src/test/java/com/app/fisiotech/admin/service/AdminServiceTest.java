package com.app.fisiotech.admin.service;

import com.app.fisiotech.admin.dto.AdminCreateRequest;
import com.app.fisiotech.admin.entity.Admin;
import com.app.fisiotech.admin.repository.AdminRepository;
import com.app.fisiotech.auth.dto.AlterarSenhaRequest;
import com.app.fisiotech.exception.EmailJaCadastradoException;
import com.app.fisiotech.exception.RecursoNaoEncontradoException;
import com.app.fisiotech.exception.SenhaAtualInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private Admin adminExistente() {
        Admin admin = new Admin("Admin", "admin@fisiotech.com", "senhaAntigaHash");
        try {
            var field = Admin.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(admin, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return admin;
    }

    @Test
    void naoDevePermitirCadastrarComEmailJaExistente() {
        when(adminRepository.existsByEmail("admin@fisiotech.com")).thenReturn(true);

        AdminCreateRequest request = new AdminCreateRequest("Admin", "admin@fisiotech.com", "senha123");

        assertThatThrownBy(() -> adminService.criar(request))
                .isInstanceOf(EmailJaCadastradoException.class);

        verify(adminRepository, never()).save(any());
    }

    @Test
    void devePermitirCadastrarAdminValido() {
        when(adminRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash");
        when(adminRepository.save(any(Admin.class))).thenAnswer(inv -> inv.getArgument(0));

        AdminCreateRequest request = new AdminCreateRequest("Admin", "admin@fisiotech.com", "senha123");

        Admin resultado = adminService.criar(request);

        assertThat(resultado.getEmail()).isEqualTo("admin@fisiotech.com");
        assertThat(resultado.getSenha()).isEqualTo("hash");
    }

    // --- alterarSenha (autoatendimento, F-02) ---

    @Test
    void naoDevePermitirAlterarPropriaSenhaComSenhaAtualIncorreta() {
        Admin existente = adminExistente();
        when(adminRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.matches("senhaErrada", "senhaAntigaHash")).thenReturn(false);

        AlterarSenhaRequest request = new AlterarSenhaRequest("senhaErrada", "novaSenha123");

        assertThatThrownBy(() -> adminService.alterarSenha(1L, request))
                .isInstanceOf(SenhaAtualInvalidaException.class);

        verify(adminRepository, never()).save(any());
    }

    @Test
    void devePermitirAlterarPropriaSenhaComSenhaAtualCorreta() {
        Admin existente = adminExistente();
        when(adminRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.matches("senhaAntiga123", "senhaAntigaHash")).thenReturn(true);
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novoHash");

        AlterarSenhaRequest request = new AlterarSenhaRequest("senhaAntiga123", "novaSenha123");

        adminService.alterarSenha(1L, request);

        assertThat(existente.getSenha()).isEqualTo("novoHash");
        verify(adminRepository).save(existente);
    }

    @Test
    void deveLancarNaoEncontradoAoAlterarSenhaDeAdminInexistente() {
        when(adminRepository.findById(99L)).thenReturn(Optional.empty());

        AlterarSenhaRequest request = new AlterarSenhaRequest("senhaAtual", "novaSenha123");

        assertThatThrownBy(() -> adminService.alterarSenha(99L, request))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}
