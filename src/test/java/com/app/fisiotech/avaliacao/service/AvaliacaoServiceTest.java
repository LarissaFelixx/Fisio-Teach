package com.app.fisiotech.avaliacao.service;

import com.app.fisiotech.avaliacao.dto.AvaliacaoCreateRequest;
import com.app.fisiotech.avaliacao.entity.Avaliacao;
import com.app.fisiotech.avaliacao.repository.AvaliacaoRepository;
import com.app.fisiotech.consulta.entity.Consulta;
import com.app.fisiotech.consulta.entity.StatusConsulta;
import com.app.fisiotech.consulta.entity.TipoConsulta;
import com.app.fisiotech.consulta.repository.ConsultaRepository;
import com.app.fisiotech.exception.EstadoInvalidoException;
import com.app.fisiotech.exception.RecursoDuplicadoException;
import com.app.fisiotech.exception.RecursoNaoEncontradoException;
import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.profissional.entity.Profissional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    private Profissional profissional;
    private Paciente paciente;
    private Consulta consulta;

    @BeforeEach
    void setUp() {
        profissional = new Profissional("Ana Souza", "ana@fisiotech.com", "hash", "CREFITO-1", "Ortopedia");
        setId(profissional, 1L);

        paciente = new Paciente("Joao Silva", "joao@paciente.com", "hash", profissional);
        setId(paciente, 1L);

        consulta = new Consulta(paciente, profissional, LocalDateTime.now(), TipoConsulta.PRESENCIAL, null, null);
        setId(consulta, 1L);
    }

    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --- criarComoPaciente: a regra corrigida (consulta precisa estar REALIZADA) ---

    @Test
    void naoDevePermitirAvaliarConsultaAindaAgendada() {
        consulta.setStatus(StatusConsulta.AGENDADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        AvaliacaoCreateRequest request = new AvaliacaoCreateRequest(1L, 5, "Otimo");

        assertThatThrownBy(() -> avaliacaoService.criarComoPaciente(request, 1L))
                .isInstanceOf(EstadoInvalidoException.class)
                .hasMessage("Só é possível avaliar consultas já realizadas.");

        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirAvaliarConsultaCancelada() {
        consulta.setStatus(StatusConsulta.CANCELADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        AvaliacaoCreateRequest request = new AvaliacaoCreateRequest(1L, 5, "Otimo");

        assertThatThrownBy(() -> avaliacaoService.criarComoPaciente(request, 1L))
                .isInstanceOf(EstadoInvalidoException.class);

        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void devePermitirAvaliarConsultaRealizada() {
        consulta.setStatus(StatusConsulta.REALIZADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(avaliacaoRepository.existsByConsultaId(1L)).thenReturn(false);
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(inv -> inv.getArgument(0));

        AvaliacaoCreateRequest request = new AvaliacaoCreateRequest(1L, 5, "Otimo atendimento");

        Avaliacao resultado = avaliacaoService.criarComoPaciente(request, 1L);

        assertThat(resultado.getNota()).isEqualTo(5);
        assertThat(resultado.getComentario()).isEqualTo("Otimo atendimento");
        verify(avaliacaoRepository).save(any(Avaliacao.class));
    }

    @Test
    void naoDevePermitirAvaliarDuasVezesAMesmaConsulta() {
        consulta.setStatus(StatusConsulta.REALIZADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(avaliacaoRepository.existsByConsultaId(1L)).thenReturn(true);

        AvaliacaoCreateRequest request = new AvaliacaoCreateRequest(1L, 5, "Otimo");

        assertThatThrownBy(() -> avaliacaoService.criarComoPaciente(request, 1L))
                .isInstanceOf(RecursoDuplicadoException.class);

        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirPacienteAvaliarConsultaDeOutroPaciente() {
        consulta.setStatus(StatusConsulta.REALIZADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        AvaliacaoCreateRequest request = new AvaliacaoCreateRequest(1L, 5, "Otimo");

        // pacienteId 2L != paciente dono da consulta (1L)
        assertThatThrownBy(() -> avaliacaoService.criarComoPaciente(request, 2L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Consulta não encontrada.");

        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void deveLancarNaoEncontradoQuandoConsultaNaoExiste() {
        when(consultaRepository.findById(99L)).thenReturn(Optional.empty());

        AvaliacaoCreateRequest request = new AvaliacaoCreateRequest(99L, 5, "Otimo");

        assertThatThrownBy(() -> avaliacaoService.criarComoPaciente(request, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // --- criar (profissional): mesma regra deve valer ---

    @Test
    void naoDevePermitirProfissionalAvaliarConsultaAindaAgendada() {
        consulta.setStatus(StatusConsulta.AGENDADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        AvaliacaoCreateRequest request = new AvaliacaoCreateRequest(1L, 5, "Otimo");

        assertThatThrownBy(() -> avaliacaoService.criar(request, 1L))
                .isInstanceOf(EstadoInvalidoException.class);

        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirProfissionalAvaliarConsultaDeOutroProfissional() {
        consulta.setStatus(StatusConsulta.REALIZADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        AvaliacaoCreateRequest request = new AvaliacaoCreateRequest(1L, 5, "Otimo");

        // profissionalId 2L != dono do paciente da consulta (1L)
        assertThatThrownBy(() -> avaliacaoService.criar(request, 2L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Consulta não encontrada.");
    }

    // --- buscarPorConsultaEPaciente ---

    @Test
    void deveLancarNaoEncontradoAoBuscarAvaliacaoInexistente() {
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(avaliacaoRepository.findByConsultaId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avaliacaoService.buscarPorConsultaEPaciente(1L, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Essa consulta ainda não foi avaliada.");
    }
}
