package com.app.fisiotech.mensagem.service;

import com.app.fisiotech.consulta.entity.Consulta;
import com.app.fisiotech.consulta.repository.ConsultaRepository;
import com.app.fisiotech.exception.RecursoNaoEncontradoException;
import com.app.fisiotech.mensagem.dto.CaixaEntradaItemResponse;
import com.app.fisiotech.mensagem.dto.MensagemCreateRequest;
import com.app.fisiotech.mensagem.dto.MinhaConversaResponse;
import com.app.fisiotech.mensagem.entity.AutorMensagem;
import com.app.fisiotech.mensagem.entity.Mensagem;
import com.app.fisiotech.mensagem.repository.MensagemRepository;
import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.entity.Profissional;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MensagemService {

    private final MensagemRepository mensagemRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ConsultaRepository consultaRepository;

    @Transactional
    public Mensagem enviar(MensagemCreateRequest request, Long profissionalId) {
        Paciente paciente = buscarPaciente(request.pacienteId());
        Profissional profissional = buscarProfissional(profissionalId);
        validarConsultaEntre(request.pacienteId(), profissionalId);

        Mensagem mensagem = new Mensagem(paciente, profissional, request.autor(), request.conteudo());

        return mensagemRepository.save(mensagem);
    }


    @Transactional(readOnly = true)
    public List<Mensagem> listarPorPaciente(Long pacienteId, Long profissionalId) {
        validarConsultaEntre(pacienteId, profissionalId);

        return mensagemRepository.findByPacienteIdAndProfissionalId(pacienteId, profissionalId, Sort.by(Sort.Direction.ASC, "dataEnvio"));
    }


    @Transactional
    public Mensagem enviarComoPaciente(Long pacienteId, Long profissionalId, String conteudo) {
        Paciente paciente = buscarPaciente(pacienteId);
        Profissional profissional = buscarProfissional(profissionalId);
        validarConsultaEntre(pacienteId, profissionalId);

        Mensagem mensagem = new Mensagem(paciente, profissional, AutorMensagem.PACIENTE, conteudo);

        return mensagemRepository.save(mensagem);
    }


    @Transactional(readOnly = true)
    public List<Mensagem> listarConversaDoPaciente(Long pacienteId, Long profissionalId) {
        validarConsultaEntre(pacienteId, profissionalId);

        return mensagemRepository.findByPacienteIdAndProfissionalId(pacienteId, profissionalId, Sort.by(Sort.Direction.ASC, "dataEnvio"));
    }


    @Transactional(readOnly = true)
    public List<CaixaEntradaItemResponse> listarCaixaEntrada(Long profissionalId) {
        List<Paciente> pacientes = consultaRepository.findByProfissionalId(profissionalId, Sort.unsorted())
                .stream()
                .map(Consulta::getPaciente)
                .distinct()
                .toList();

        List<CaixaEntradaItemResponse> comMensagem = new ArrayList<>();
        List<CaixaEntradaItemResponse> semMensagem = new ArrayList<>();

        for (Paciente paciente : pacientes) {
            Mensagem ultima = mensagemRepository
                    .findFirstByPacienteIdAndProfissionalIdOrderByDataEnvioDesc(paciente.getId(), profissionalId)
                    .orElse(null);
            CaixaEntradaItemResponse item = CaixaEntradaItemResponse.from(paciente, ultima);
            (ultima != null ? comMensagem : semMensagem).add(item);
        }

        comMensagem.sort(Comparator.comparing(CaixaEntradaItemResponse::dataUltimaMensagem).reversed());
        semMensagem.sort(Comparator.comparing(CaixaEntradaItemResponse::pacienteNome));

        List<CaixaEntradaItemResponse> resultado = new ArrayList<>(comMensagem);
        resultado.addAll(semMensagem);
        return resultado;
    }


    @Transactional(readOnly = true)
    public List<MinhaConversaResponse> listarConversasDoPaciente(Long pacienteId) {
        List<Profissional> profissionais = consultaRepository.findByPaciente_Id(pacienteId, Sort.unsorted())
                .stream()
                .map(Consulta::getProfissional)
                .distinct()
                .toList();

        List<MinhaConversaResponse> comMensagem = new ArrayList<>();
        List<MinhaConversaResponse> semMensagem = new ArrayList<>();

        for (Profissional profissional : profissionais) {
            Mensagem ultima = mensagemRepository
                    .findFirstByPacienteIdAndProfissionalIdOrderByDataEnvioDesc(pacienteId, profissional.getId())
                    .orElse(null);
            MinhaConversaResponse item = MinhaConversaResponse.from(profissional, ultima);
            (ultima != null ? comMensagem : semMensagem).add(item);
        }

        comMensagem.sort(Comparator.comparing(MinhaConversaResponse::dataUltimaMensagem).reversed());
        semMensagem.sort(Comparator.comparing(MinhaConversaResponse::profissionalNome));

        List<MinhaConversaResponse> resultado = new ArrayList<>(comMensagem);
        resultado.addAll(semMensagem);
        return resultado;
    }


    private void validarConsultaEntre(Long pacienteId, Long profissionalId) {
        if (!consultaRepository.existsByPacienteIdAndProfissionalId(pacienteId, profissionalId)) {
            throw new RecursoNaoEncontradoException("Paciente não encontrado.");
        }
    }


    private Paciente buscarPaciente(Long pacienteId) {
        return pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }


    private Profissional buscarProfissional(Long profissionalId) {
        return profissionalRepository.findById(profissionalId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado."));
    }
}
