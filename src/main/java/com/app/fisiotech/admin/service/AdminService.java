package com.app.fisiotech.admin.service;

import com.app.fisiotech.admin.dto.AdminCreateRequest;
import com.app.fisiotech.admin.entity.Admin;
import com.app.fisiotech.admin.repository.AdminRepository;
import com.app.fisiotech.auth.dto.AlterarSenhaRequest;
import com.app.fisiotech.exception.EmailJaCadastradoException;
import com.app.fisiotech.exception.RecursoNaoEncontradoException;
import com.app.fisiotech.exception.SenhaAtualInvalidaException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Admin criar(AdminCreateRequest request){
      String emailNormalizado = normalizarEmail(request.email());

      if(adminRepository.existsByEmail(emailNormalizado)){
          throw new EmailJaCadastradoException("Já existe administrador cadastrado com este email.");
      }

      String senhaCriptografada = passwordEncoder.encode(request.senha());

      Admin admin = new Admin(
              request.nome(),
              emailNormalizado,
              senhaCriptografada
      );

      return adminRepository.save(admin);
    }


    @Transactional
    public void alterarSenha(Long adminId, AlterarSenhaRequest request) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Admin não encontrado."));

        if (!passwordEncoder.matches(request.senhaAtual(), admin.getSenha())) {
            throw new SenhaAtualInvalidaException("Senha atual incorreta.");
        }

        admin.setSenha(passwordEncoder.encode(request.novaSenha()));
        adminRepository.save(admin);
    }


    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

}
