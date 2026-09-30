package com.app.fisiotech.auth.security;

import com.app.fisiotech.admin.entity.Admin;
import com.app.fisiotech.admin.repository.AdminRepository;
import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.entity.Profissional;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private static final String ROLE_PROFISSIONAL = "ROLE_PROFISSIONAL";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_PACIENTE = "ROLE_PACIENTE";

    private final ProfissionalRepository profissionalRepository;
    private final AdminRepository adminRepository;
    private final PacienteRepository pacienteRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

        return profissionalRepository.findByEmail(emailNormalizado)
                .map(this::toAuthenticatedUser)
                .or(() -> adminRepository.findByEmail(emailNormalizado).map(this::toAuthenticatedUser))
                .or(() -> pacienteRepository.findByEmail(emailNormalizado).map(this::toAuthenticatedUser))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
    }

    private AuthenticatedUser toAuthenticatedUser(Profissional profissional) {
        return new AuthenticatedUser(profissional.getId(), profissional.getNome(), profissional.getEmail(), profissional.getSenha(), ROLE_PROFISSIONAL);
    }

    private AuthenticatedUser toAuthenticatedUser(Admin admin) {
        return new AuthenticatedUser(admin.getId(), admin.getNome(), admin.getEmail(), admin.getSenha(), ROLE_ADMIN);
    }

    private AuthenticatedUser toAuthenticatedUser(Paciente paciente) {
        return new AuthenticatedUser(paciente.getId(), paciente.getNome(), paciente.getEmail(), paciente.getSenha(), ROLE_PACIENTE);
    }

}
