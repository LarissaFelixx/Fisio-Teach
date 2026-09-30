package com.app.fisiotech.admin.config;

import com.app.fisiotech.admin.dto.AdminCreateRequest;
import com.app.fisiotech.admin.repository.AdminRepository;
import com.app.fisiotech.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final AdminService adminService;
    private final AdminRepository adminRepository;

    @Value("${app.admin.nome}")
    private String nomeAdmin;

    @Value("${app.admin.email}")
    private String emailAdmin;

    @Value("${app.admin.senha}")
    private String senhaAdmin;

    @Override
    public void run(String... args) {
        if (adminRepository.count() > 0) {
            return;
        }

        AdminCreateRequest request = new AdminCreateRequest(
                nomeAdmin,
                emailAdmin,
                senhaAdmin
        );

        adminService.criar(request);
    }



}
