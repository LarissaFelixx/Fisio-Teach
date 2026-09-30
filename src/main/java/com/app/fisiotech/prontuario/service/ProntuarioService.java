package com.app.fisiotech.prontuario.service;

import com.app.fisiotech.consulta.repository.ConsultaRepository;
import com.app.fisiotech.exception.*;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import com.app.fisiotech.prontuario.dto.*;
import com.app.fisiotech.prontuario.entity.*;
import com.app.fisiotech.prontuario.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
public class ProntuarioService {
    private final PacienteRepository pacientes;
    private final ProfissionalRepository profissionais;
    private final ConsultaRepository consultas;
    private final EvolucaoClinicaRepository evolucoes;
    private final PlanoTerapeuticoRepository planos;

    @Transactional
    public EvolucaoClinica registrarEvolucao(Long pacienteId, Long profissionalId, EvolucaoRequest request) {
        var paciente = pacienteDoProfissional(pacienteId, profissionalId);
        var profissional = profissionais.getReferenceById(profissionalId);
        var consulta = request.consultaId() == null ? null : consultas.findById(request.consultaId())
                .filter(c -> c.getPaciente().getId().equals(pacienteId) && c.getProfissional().getId().equals(profissionalId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));
        return evolucoes.save(new EvolucaoClinica(paciente, profissional, consulta, request.observacoes(),
                request.procedimentos(), request.respostaPaciente(), request.conduta(), request.visivelPaciente()));
    }

    @Transactional(readOnly=true)
    public List<EvolucaoClinica> evolucoes(Long pacienteId, Long profissionalId) {
        pacienteDoProfissional(pacienteId, profissionalId);
        return evolucoes.findByPacienteIdAndProfissionalIdOrderByDataRegistroDesc(pacienteId, profissionalId);
    }

    @Transactional
    public PlanoTerapeutico revisarPlano(Long pacienteId, Long profissionalId, PlanoRequest request) {
        if (request.dataFimPrevista()!=null && request.dataFimPrevista().isBefore(request.dataInicio()))
            throw new EstadoInvalidoException("A data final não pode ser anterior à data inicial.");
        var paciente = pacienteDoProfissional(pacienteId, profissionalId);
        var anterior = planos.findFirstByPacienteIdAndProfissionalIdOrderByRevisaoDesc(pacienteId, profissionalId);
        anterior.filter(p -> p.getStatus()==StatusPlano.ATIVO).ifPresent(PlanoTerapeutico::substituir);
        int revisao = anterior.map(p -> p.getRevisao()+1).orElse(1);
        return planos.save(new PlanoTerapeutico(paciente, profissionais.getReferenceById(profissionalId), revisao,
                request.objetivos(), request.condutas(), request.dataInicio(), request.dataFimPrevista(), request.visivelPaciente()));
    }

    @Transactional(readOnly=true)
    public List<PlanoTerapeutico> planos(Long pacienteId, Long profissionalId) {
        pacienteDoProfissional(pacienteId, profissionalId);
        return planos.findByPacienteIdAndProfissionalIdOrderByRevisaoDesc(pacienteId, profissionalId);
    }

    @Transactional(readOnly=true)
    public List<EvolucaoClinica> evolucoesVisiveis(Long pacienteId) {
        pacienteExistente(pacienteId); return evolucoes.findByPacienteIdAndVisivelPacienteTrueOrderByDataRegistroDesc(pacienteId);
    }
    @Transactional(readOnly=true)
    public List<PlanoTerapeutico> planosVisiveis(Long pacienteId) {
        pacienteExistente(pacienteId); return planos.findByPacienteIdAndVisivelPacienteTrueOrderByRevisaoDesc(pacienteId);
    }

    private com.app.fisiotech.paciente.entity.Paciente pacienteDoProfissional(Long id, Long profissionalId) {
        var p = pacienteExistente(id);
        if (p.getProfissional()==null || !p.getProfissional().getId().equals(profissionalId))
            throw new RecursoNaoEncontradoException("Paciente não encontrado.");
        return p;
    }
    private com.app.fisiotech.paciente.entity.Paciente pacienteExistente(Long id) {
        return pacientes.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));
    }
}
