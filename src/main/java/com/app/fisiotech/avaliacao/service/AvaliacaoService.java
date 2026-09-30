package com.app.fisiotech.avaliacao.service;

import com.app.fisiotech.avaliacao.dto.AvaliacaoCreateRequest;
import com.app.fisiotech.avaliacao.entity.Avaliacao;
import com.app.fisiotech.avaliacao.repository.AvaliacaoRepository;
import com.app.fisiotech.consulta.entity.Consulta;
import com.app.fisiotech.consulta.entity.StatusConsulta;
import com.app.fisiotech.consulta.repository.ConsultaRepository;
import com.app.fisiotech.exception.EstadoInvalidoException;
import com.app.fisiotech.exception.RecursoDuplicadoException;
import com.app.fisiotech.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final ConsultaRepository consultaRepository;

    @Transactional
    public Avaliacao criar(AvaliacaoCreateRequest request, Long profissionalId) {
        Consulta consulta = buscarConsultaDoProfissional(request.consultaId(), profissionalId);
        validarConsultaRealizada(consulta);

        if (avaliacaoRepository.existsByConsultaId(consulta.getId())) {
            throw new RecursoDuplicadoException("Essa consulta já foi avaliada.");
        }

        Avaliacao avaliacao = new Avaliacao(consulta, request.nota(), request.comentario());

        return avaliacaoRepository.save(avaliacao);
    }


    @Transactional(readOnly = true)
    public Avaliacao buscarPorConsulta(Long consultaId, Long profissionalId) {
        buscarConsultaDoProfissional(consultaId, profissionalId);

        return avaliacaoRepository.findByConsultaId(consultaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Essa consulta ainda não foi avaliada."));
    }


    @Transactional
    public Avaliacao criarComoPaciente(AvaliacaoCreateRequest request, Long pacienteId) {
        Consulta consulta = buscarConsultaDoPaciente(request.consultaId(), pacienteId);
        validarConsultaRealizada(consulta);

        if (avaliacaoRepository.existsByConsultaId(consulta.getId())) {
            throw new RecursoDuplicadoException("Essa consulta já foi avaliada.");
        }

        Avaliacao avaliacao = new Avaliacao(consulta, request.nota(), request.comentario());

        return avaliacaoRepository.save(avaliacao);
    }


    @Transactional(readOnly = true)
    public Avaliacao buscarPorConsultaEPaciente(Long consultaId, Long pacienteId) {
        buscarConsultaDoPaciente(consultaId, pacienteId);

        return avaliacaoRepository.findByConsultaId(consultaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Essa consulta ainda não foi avaliada."));
    }


    private void validarConsultaRealizada(Consulta consulta) {
        if (consulta.getStatus() != StatusConsulta.REALIZADA) {
            throw new EstadoInvalidoException("Só é possível avaliar consultas já realizadas.");
        }
    }


    private Consulta buscarConsultaDoProfissional(Long consultaId, Long profissionalId) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));

        if (consulta.getPaciente().getProfissional() == null
                || !consulta.getPaciente().getProfissional().getId().equals(profissionalId)) {
            throw new RecursoNaoEncontradoException("Consulta não encontrada.");
        }

        return consulta;
    }


    private Consulta buscarConsultaDoPaciente(Long consultaId, Long pacienteId) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));

        if (!consulta.getPaciente().getId().equals(pacienteId)) {
            throw new RecursoNaoEncontradoException("Consulta não encontrada.");
        }

        return consulta;
    }
}
