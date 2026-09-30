package com.app.fisiotech.profissional.service;

import com.app.fisiotech.auth.dto.AlterarSenhaRequest;
import com.app.fisiotech.exception.EmailJaCadastradoException;
import com.app.fisiotech.exception.RecursoNaoEncontradoException;
import com.app.fisiotech.exception.SenhaAtualInvalidaException;
import com.app.fisiotech.profissional.dto.ProfissionalCreateRequest;
import com.app.fisiotech.profissional.dto.ProfissionalUpdateRequest;
import com.app.fisiotech.profissional.entity.Profissional;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Profissional criar(ProfissionalCreateRequest request) {
        String emailNormalizado = normalizarEmail(request.email());

        if (profissionalRepository.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException("Já existe um profissional cadastrado com este email.");
        }

        if (profissionalRepository.existsByRegistroProfissional(request.registroProfissional())) {
            throw new EmailJaCadastradoException("Já existe um profissional cadastrado com este registro profissional.");
        }

        String senhaCriptografada = passwordEncoder.encode(request.senha());

        Profissional profissional = new Profissional(
                request.nome().trim(),
                emailNormalizado,
                senhaCriptografada,
                request.registroProfissional().trim(),
                request.especialidade().trim()
        );

        profissional.setValorConsultaParticular(request.valorConsultaParticular());
        profissional.setConveniosAceitos(request.conveniosAceitos() != null ? request.conveniosAceitos() : new ArrayList<>());
        profissional.setFoto(request.foto());
        profissional.setDataNascimento(request.dataNascimento());
        profissional.setSexo(request.sexo());
        profissional.setTelefone(request.telefone());

        return profissionalRepository.save(profissional);
    }


    @Transactional(readOnly = true)
    public List<Profissional> listarTodos() {
        return profissionalRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }


    @Transactional(readOnly = true)
    public Profissional buscarPorId(Long id) {
        return profissionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado."));
    }


    @Transactional
    public Profissional atualizar(Long id, ProfissionalUpdateRequest request) {
        Profissional profissionalASerAtualizado = buscarPorId(id);

        String emailNormalizado = normalizarEmail(request.email());

        if (profissionalRepository.existsByEmailAndIdNot(emailNormalizado, id)) {
            throw new EmailJaCadastradoException("Email já cadastrado.");
        }

        if (profissionalRepository.existsByRegistroProfissionalAndIdNot(request.registroProfissional(), id)) {
            throw new EmailJaCadastradoException("Registro profissional já cadastrado.");
        }

        profissionalASerAtualizado.setNome(request.nome().trim());
        profissionalASerAtualizado.setEmail(emailNormalizado);
        if (request.senha() != null && !request.senha().isBlank()) {
            profissionalASerAtualizado.setSenha(passwordEncoder.encode(request.senha()));
        }
        profissionalASerAtualizado.setRegistroProfissional(request.registroProfissional().trim());
        profissionalASerAtualizado.setEspecialidade(request.especialidade().trim());
        profissionalASerAtualizado.setValorConsultaParticular(request.valorConsultaParticular());
        profissionalASerAtualizado.setConveniosAceitos(request.conveniosAceitos() != null ? request.conveniosAceitos() : new ArrayList<>());
        profissionalASerAtualizado.setFoto(request.foto());
        profissionalASerAtualizado.setDataNascimento(request.dataNascimento());
        profissionalASerAtualizado.setSexo(request.sexo());
        profissionalASerAtualizado.setTelefone(request.telefone());

        return profissionalRepository.save(profissionalASerAtualizado);
    }


    @Transactional(readOnly = true)
    public List<Profissional> buscarPublico(String nome, String especialidade) {
        String nomeFiltro = nome != null ? nome.trim().toLowerCase(Locale.ROOT) : null;
        String especialidadeFiltro = especialidade != null ? especialidade.trim().toLowerCase(Locale.ROOT) : null;

        return profissionalRepository.findAll(Sort.by(Sort.Direction.ASC, "nome")).stream()
                .filter(p -> nomeFiltro == null || nomeFiltro.isBlank() || p.getNome().toLowerCase(Locale.ROOT).contains(nomeFiltro))
                .filter(p -> especialidadeFiltro == null || especialidadeFiltro.isBlank() || p.getEspecialidade().toLowerCase(Locale.ROOT).contains(especialidadeFiltro))
                .toList();
    }


    @Transactional
    public void alterarSenha(Long profissionalId, AlterarSenhaRequest request) {
        Profissional profissional = buscarPorId(profissionalId);

        if (!passwordEncoder.matches(request.senhaAtual(), profissional.getSenha())) {
            throw new SenhaAtualInvalidaException("Senha atual incorreta.");
        }

        profissional.setSenha(passwordEncoder.encode(request.novaSenha()));
        profissionalRepository.save(profissional);
    }


    @Transactional
    public void deletar(Long id) {
        Profissional profissionalASerDeletado = buscarPorId(id);
        profissionalRepository.delete(profissionalASerDeletado);
    }


    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
