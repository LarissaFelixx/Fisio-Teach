package com.app.fisiotech.auth.config;

import com.app.fisiotech.admin.repository.AdminRepository;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import com.app.fisiotech.auth.service.AccountSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Stops startup on ambiguous legacy emails instead of silently choosing a role. */
@Component
@Order(-100)
@RequiredArgsConstructor
public class EmailRegistryInitializer implements ApplicationRunner {
    private final AdminRepository admins;
    private final ProfissionalRepository profissionais;
    private final PacienteRepository pacientes;
    private final AccountSecurityService security;

    @Override @Transactional
    public void run(ApplicationArguments args) {
        admins.findAll().forEach(u -> security.registerEmail("ADMIN", u.getId(), u.getEmail()));
        profissionais.findAll().forEach(u -> security.registerEmail("PROFISSIONAL", u.getId(), u.getEmail()));
        pacientes.findAll().forEach(u -> security.registerEmail("PACIENTE", u.getId(), u.getEmail()));
    }
}
