package com.app.fisiotech.consulta.service;

import com.app.fisiotech.avaliacao.repository.AvaliacaoRepository;
import com.app.fisiotech.consulta.dto.ConsultaCreateRequest;
import com.app.fisiotech.consulta.dto.MeConsultaCreateRequest;
import com.app.fisiotech.consulta.dto.ConsultaUpdateRequest;
import com.app.fisiotech.consulta.entity.Consulta;
import com.app.fisiotech.consulta.entity.StatusConsulta;
import com.app.fisiotech.consulta.repository.ConsultaRepository;
import com.app.fisiotech.exception.EstadoInvalidoException;
import com.app.fisiotech.exception.HorarioIndisponivelException;
import com.app.fisiotech.exception.RecursoNaoEncontradoException;
import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.entity.Profissional;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    @Transactional
    public Consulta criar(ConsultaCreateRequest request, Long profissionalId) {
        Paciente paciente = buscarPacienteDoProfissional(request.pacienteId(), profissionalId);
        Profissional profissional = profissionalRepository.getReferenceById(profissionalId);

        Consulta consulta = new Consulta(
                paciente,
                profissional,
                request.dataHora(),
                request.tipo(),
                request.convenio(),
                request.valor()
        );

        return consultaRepository.save(consulta);
    }


    @Transactional
    public Consulta criarComoPaciente(Long pacienteId, MeConsultaCreateRequest request) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));

        Profissional profissional = profissionalRepository.findById(request.profissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional não encontrado."));

        boolean horarioOcupado = consultaRepository.existsByProfissionalIdAndDataHoraAndStatusNot(
                request.profissionalId(), request.dataHora(), StatusConsulta.CANCELADA);

        if (horarioOcupado) {
            throw new HorarioIndisponivelException("Este horário não está mais disponível.");
        }

        boolean particular = request.convenio() == null || request.convenio().isBlank();
        BigDecimal valor = particular ? profissional.getValorConsultaParticular() : null;

        Consulta consulta = new Consulta(
                paciente,
                profissional,
                request.dataHora(),
                request.tipo(),
                request.convenio(),
                valor
        );

        consulta = consultaRepository.save(consulta);

        if (paciente.getProfissional() == null) {
            paciente.setProfissional(profissional);
            pacienteRepository.save(paciente);
        }

        return consulta;
    }


    @Transactional(readOnly = true)
    public List<Consulta> listarTodos(Long profissionalId, Long pacienteId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "dataHora");

        if (pacienteId != null) {
            return consultaRepository.findByPacienteIdAndProfissionalId(pacienteId, profissionalId, sort);
        }

        return consultaRepository.findByProfissionalId(profissionalId, sort);
    }


    @Transactional(readOnly = true)
    public Consulta buscarPorId(Long id, Long profissionalId) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));

        if (!consulta.getProfissional().getId().equals(profissionalId)) {
            throw new RecursoNaoEncontradoException("Consulta não encontrada.");
        }

        return consulta;
    }


    @Transactional
    public Consulta atualizar(Long id, ConsultaUpdateRequest request, Long profissionalId) {
        Consulta consulta = buscarPorId(id, profissionalId);

        LocalDateTime dataHoraAntes = consulta.getDataHora();
        if (!dataHoraAntes.equals(request.dataHora())) {
            consulta.setFoiRemarcada(true);
        }

        consulta.setDataHora(request.dataHora());
        consulta.setTipo(request.tipo());
        consulta.setStatus(request.status());
        consulta.setConvenio(request.convenio());
        consulta.setValor(request.valor());

        if (request.quadroClinico() != null) {
            consulta.setQuadroClinico(request.quadroClinico().toEntity());
        }
        if (request.habitosVida() != null) {
            consulta.setHabitosVida(request.habitosVida().toEntity());
        }
        if (request.exameFisico() != null) {
            consulta.setExameFisico(request.exameFisico().toEntity());
        }
        if (request.diagnostico() != null) {
            consulta.setDiagnostico(request.diagnostico().toEntity());
        }

        return consultaRepository.save(consulta);
    }


    @Transactional
    public void deletar(Long id, Long profissionalId) {
        Consulta consulta = buscarPorId(id, profissionalId);
        avaliacaoRepository.findByConsultaId(id).ifPresent(avaliacaoRepository::delete);
        consultaRepository.delete(consulta);
    }


    @Transactional(readOnly = true)
    public List<Consulta> listarDoPacienteLogado(Long pacienteId) {
        return consultaRepository.findByPaciente_Id(pacienteId, Sort.by(Sort.Direction.DESC, "dataHora"));
    }


    @Transactional(readOnly = true)
    public Consulta buscarPorIdEPaciente(Long id, Long pacienteId) {
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Consulta não encontrada."));

        if (!consulta.getPaciente().getId().equals(pacienteId)) {
            throw new RecursoNaoEncontradoException("Consulta não encontrada.");
        }

        return consulta;
    }


    @Transactional
    public Consulta cancelarComoPaciente(Long id, Long pacienteId) {
        Consulta consulta = buscarPorIdEPaciente(id, pacienteId);
        validarStatusEditavel(consulta);

        consulta.setStatus(StatusConsulta.CANCELADA);
        return consultaRepository.save(consulta);
    }


    @Transactional
    public Consulta remarcarComoPaciente(Long id, Long pacienteId, LocalDateTime novaDataHora) {
        Consulta consulta = buscarPorIdEPaciente(id, pacienteId);
        validarStatusEditavel(consulta);

        boolean horarioOcupado = consultaRepository.existsByProfissionalIdAndDataHoraAndStatusNotAndIdNot(
                consulta.getProfissional().getId(), novaDataHora, StatusConsulta.CANCELADA, id);

        if (horarioOcupado) {
            throw new HorarioIndisponivelException("Este horário não está mais disponível.");
        }

        consulta.setDataHora(novaDataHora);
        consulta.setFoiRemarcada(true);
        return consultaRepository.save(consulta);
    }


    private void validarStatusEditavel(Consulta consulta) {
        if (consulta.getStatus() == StatusConsulta.REALIZADA || consulta.getStatus() == StatusConsulta.CANCELADA) {
            throw new EstadoInvalidoException("Esta consulta não pode mais ser alterada.");
        }
    }


    private Paciente buscarPacienteDoProfissional(Long pacienteId, Long profissionalId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente não encontrado."));

        if (paciente.getProfissional() == null || !paciente.getProfissional().getId().equals(profissionalId)) {
            throw new RecursoNaoEncontradoException("Paciente não encontrado.");
        }

        return paciente;
    }
}
