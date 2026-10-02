package com.app.fisiotech.auth.service;

import com.app.fisiotech.admin.repository.AdminRepository;
import com.app.fisiotech.auth.dto.RecuperarSenhaRequest;
import com.app.fisiotech.auth.dto.RedefinirSenhaRequest;
import com.app.fisiotech.auth.entity.CodigoRecuperacaoSenha;
import com.app.fisiotech.auth.repository.CodigoRecuperacaoSenhaRepository;
import com.app.fisiotech.exception.CodigoRecuperacaoInvalidoException;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class RecuperacaoSenhaService {

    static final int MAX_TENTATIVAS = 5;

    private final CodigoRecuperacaoSenhaRepository codigoRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AdminRepository adminRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnvioCodigoRecuperacaoService envioCodigo;
    private final AccountSecurityService accountSecurity;
    private final Clock clock;
    private final long validadeMinutos;
    private final SecureRandom random = new SecureRandom();

    public RecuperacaoSenhaService(
            CodigoRecuperacaoSenhaRepository codigoRepository,
            ProfissionalRepository profissionalRepository,
            AdminRepository adminRepository,
            PacienteRepository pacienteRepository,
            PasswordEncoder passwordEncoder,
            EnvioCodigoRecuperacaoService envioCodigo,
            AccountSecurityService accountSecurity,
            Clock clock,
            @Value("${app.recuperacao-senha.validade-minutos:15}") long validadeMinutos
    ) {
        this.codigoRepository = codigoRepository;
        this.profissionalRepository = profissionalRepository;
        this.adminRepository = adminRepository;
        this.pacienteRepository = pacienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.envioCodigo = envioCodigo;
        this.accountSecurity = accountSecurity;
        this.clock = clock;
        this.validadeMinutos = validadeMinutos;
    }

    @Transactional
    public void solicitarCodigo(RecuperarSenhaRequest request) {
        String email = normalizarEmail(request.email());

        // Email desconhecido: sai em silêncio. O controller responde 204 nos dois casos,
        // então ninguém consegue usar este endpoint para descobrir quem tem conta.
        if (!existeUsuario(email)) {
            return;
        }

        // Um código ativo por vez: pedir de novo invalida o anterior (e zera as tentativas
        // junto, mas o atacante não ganha nada com isso - o código novo é outro sorteio).
        codigoRepository.deleteByEmail(email);

        String codigo = "%06d".formatted(random.nextInt(1_000_000));
        LocalDateTime agora = LocalDateTime.now(clock);
        codigoRepository.save(new CodigoRecuperacaoSenha(
                email,
                passwordEncoder.encode(codigo),
                agora,
                agora.plusMinutes(validadeMinutos)
        ));

        envioCodigo.enviar(email, codigo);
    }

    // noRollbackFor: quando o código está errado lançamos exceção, mas o incremento de
    // tentativas precisa ir para o banco mesmo assim - senão o limite de 5 nunca seria atingido.
    @Transactional(noRollbackFor = CodigoRecuperacaoInvalidoException.class)
    public void redefinirSenha(RedefinirSenhaRequest request) {
        String email = normalizarEmail(request.email());

        CodigoRecuperacaoSenha codigo = codigoRepository.findFirstByEmailOrderByIdDesc(email)
                .orElseThrow(CodigoRecuperacaoInvalidoException::new);

        validarCodigo(codigo, request.codigo(), LocalDateTime.now(clock));

        String novaSenhaHash = passwordEncoder.encode(request.novaSenha());
        if (!atualizarSenhaDoUsuario(email, novaSenhaHash)) {
            // Conta removida entre o pedido e a redefinição.
            throw new CodigoRecuperacaoInvalidoException();
        }

        codigo.marcarComoUsado();
    }

    private void validarCodigo(CodigoRecuperacaoSenha codigo, String codigoInformado, LocalDateTime agora) {
        // Estados terminais primeiro: um código usado, expirado ou bloqueado é recusado mesmo
        // que o valor digitado esteja certo - e sem gastar um BCrypt nem contar tentativa.
        if (codigo.isUsado()
                || agora.isAfter(codigo.getExpiraEm())
                || codigo.getTentativas() >= MAX_TENTATIVAS) {
            throw new CodigoRecuperacaoInvalidoException();
        }

        if (!passwordEncoder.matches(codigoInformado, codigo.getCodigoHash())) {
            codigo.registrarTentativaInvalida();
            throw new CodigoRecuperacaoInvalidoException();
        }
    }

    // Mesma precedência do AppUserDetailsService (profissional > admin > paciente), para que
    // a senha alterada seja exatamente a que o login vai conferir.
    private boolean existeUsuario(String email) {
        return profissionalRepository.existsByEmail(email)
                || adminRepository.existsByEmail(email)
                || pacienteRepository.existsByEmail(email);
    }

    private boolean atualizarSenhaDoUsuario(String email, String novaSenhaHash) {
        // Como na troca de senha logado, a redefinição derruba todas as sessões JWT da conta:
        // se a senha vazou, quem estava usando a conta perde o acesso na hora.
        var profissional = profissionalRepository.findByEmail(email);
        if (profissional.isPresent()) {
            profissional.get().setSenha(novaSenhaHash);
            accountSecurity.revoke("PROFISSIONAL", profissional.get().getId());
            return true;
        }

        var admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            admin.get().setSenha(novaSenhaHash);
            accountSecurity.revoke("ADMIN", admin.get().getId());
            return true;
        }

        var paciente = pacienteRepository.findByEmail(email);
        if (paciente.isPresent()) {
            paciente.get().setSenha(novaSenhaHash);
            accountSecurity.revoke("PACIENTE", paciente.get().getId());
            return true;
        }

        return false;
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

}
