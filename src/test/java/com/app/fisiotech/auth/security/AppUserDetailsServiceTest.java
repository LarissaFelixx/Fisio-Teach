package com.app.fisiotech.auth.security;

import com.app.fisiotech.admin.entity.Admin;
import com.app.fisiotech.admin.repository.AdminRepository;
import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.entity.Profissional;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private AppUserDetailsService service;

    @Test
    void deveResolverProfissionalComoRoleProfissional() {
        Profissional profissional = new Profissional("Ana Souza", "ana@fisiotech.com", "hash", "CREFITO-1", "Ortopedia");
        when(profissionalRepository.findByEmail("ana@fisiotech.com")).thenReturn(Optional.of(profissional));

        UserDetails resultado = service.loadUserByUsername("ana@fisiotech.com");

        assertThat(resultado.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_PROFISSIONAL");
    }

    @Test
    void deveResolverAdminComoRoleAdminQuandoNaoEhProfissional() {
        lenient().when(profissionalRepository.findByEmail("admin@fisiotech.com")).thenReturn(Optional.empty());
        Admin admin = new Admin("Admin", "admin@fisiotech.com", "hash");
        when(adminRepository.findByEmail("admin@fisiotech.com")).thenReturn(Optional.of(admin));

        UserDetails resultado = service.loadUserByUsername("admin@fisiotech.com");

        assertThat(resultado.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_ADMIN");
    }

    @Test
    void deveResolverPacienteComoRolePacienteQuandoNaoEhProfissionalNemAdmin() {
        lenient().when(profissionalRepository.findByEmail("joao@paciente.com")).thenReturn(Optional.empty());
        lenient().when(adminRepository.findByEmail("joao@paciente.com")).thenReturn(Optional.empty());
        Paciente paciente = new Paciente("Joao Silva", "joao@paciente.com", "hash", null);
        when(pacienteRepository.findByEmail("joao@paciente.com")).thenReturn(Optional.of(paciente));

        UserDetails resultado = service.loadUserByUsername("joao@paciente.com");

        assertThat(resultado.getAuthorities()).extracting(Object::toString).containsExactly("ROLE_PACIENTE");
    }

    @Test
    void deveLancarUsernameNotFoundQuandoEmailNaoExisteEmNenhumPapel() {
        lenient().when(profissionalRepository.findByEmail("ninguem@nada.com")).thenReturn(Optional.empty());
        lenient().when(adminRepository.findByEmail("ninguem@nada.com")).thenReturn(Optional.empty());
        lenient().when(pacienteRepository.findByEmail("ninguem@nada.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ninguem@nada.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void deveNormalizarEmailParaMinusculoAntesDeBuscar() {
        Profissional profissional = new Profissional("Ana Souza", "ana@fisiotech.com", "hash", "CREFITO-1", "Ortopedia");
        when(profissionalRepository.findByEmail("ana@fisiotech.com")).thenReturn(Optional.of(profissional));

        UserDetails resultado = service.loadUserByUsername("  ANA@Fisiotech.COM  ");

        assertThat(resultado.getUsername()).isEqualTo("ana@fisiotech.com");
    }
}
