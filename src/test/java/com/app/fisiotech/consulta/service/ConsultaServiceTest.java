package com.app.fisiotech.consulta.service;

import com.app.fisiotech.avaliacao.repository.AvaliacaoRepository;
import com.app.fisiotech.consulta.dto.MeConsultaCreateRequest;
import com.app.fisiotech.consulta.entity.Consulta;
import com.app.fisiotech.consulta.entity.StatusConsulta;
import com.app.fisiotech.consulta.entity.TipoConsulta;
import com.app.fisiotech.consulta.repository.ConsultaRepository;
import com.app.fisiotech.exception.EstadoInvalidoException;
import com.app.fisiotech.exception.HorarioIndisponivelException;
import com.app.fisiotech.exception.RecursoNaoEncontradoException;
import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.paciente.repository.PacienteRepository;
import com.app.fisiotech.profissional.entity.Profissional;
import com.app.fisiotech.profissional.repository.ProfissionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @InjectMocks
    private ConsultaService consultaService;

    private Profissional profissional;
    private Paciente paciente;
    private Consulta consulta;
    private final LocalDateTime horario = LocalDateTime.of(2026, 8, 25, 10, 0);

    @BeforeEach
    void setUp() {
        profissional = new Profissional("Ana Souza", "ana@fisiotech.com", "hash", "CREFITO-1", "Ortopedia");
        setId(profissional, 1L);
        profissional.setValorConsultaParticular(new BigDecimal("150.00"));

        paciente = new Paciente("Joao Silva", "joao@paciente.com", "hash", null);
        setId(paciente, 1L);

        consulta = new Consulta(paciente, profissional, horario, TipoConsulta.PRESENCIAL, null, new BigDecimal("150.00"));
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

    // --- criarComoPaciente ---

    @Test
    void naoDevePermitirAgendarEmHorarioJaOcupado() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.existsByProfissionalIdAndDataHoraAndStatusNot(1L, horario, StatusConsulta.CANCELADA))
                .thenReturn(true);

        MeConsultaCreateRequest request = new MeConsultaCreateRequest(1L, horario, TipoConsulta.PRESENCIAL, null);

        assertThatThrownBy(() -> consultaService.criarComoPaciente(1L, request))
                .isInstanceOf(HorarioIndisponivelException.class);

        verify(consultaRepository, never()).save(any());
    }

    @Test
    void deveVincularProfissionalAoPacienteNaPrimeiraConsulta() {
        // paciente autocadastrado, ainda sem profissional (profissional == null no setUp)
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.existsByProfissionalIdAndDataHoraAndStatusNot(1L, horario, StatusConsulta.CANCELADA))
                .thenReturn(false);
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        MeConsultaCreateRequest request = new MeConsultaCreateRequest(1L, horario, TipoConsulta.PRESENCIAL, null);

        consultaService.criarComoPaciente(1L, request);

        assertThat(paciente.getProfissional()).isEqualTo(profissional);
        verify(pacienteRepository).save(paciente);
    }

    @Test
    void naoDeveSubstituirProfissionalJaVinculadoAoAgendarNovaConsulta() {
        Profissional outroProfissional = new Profissional("Carlos Reis", "carlos@fisiotech.com", "hash", "CREFITO-2", "Neurologia");
        setId(outroProfissional, 2L);
        paciente.setProfissional(outroProfissional);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.existsByProfissionalIdAndDataHoraAndStatusNot(1L, horario, StatusConsulta.CANCELADA))
                .thenReturn(false);
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        MeConsultaCreateRequest request = new MeConsultaCreateRequest(1L, horario, TipoConsulta.PRESENCIAL, null);

        consultaService.criarComoPaciente(1L, request);

        assertThat(paciente.getProfissional()).isEqualTo(outroProfissional);
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void deveUsarValorParticularQuandoNaoInformaConvenio() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.existsByProfissionalIdAndDataHoraAndStatusNot(any(), any(), any())).thenReturn(false);
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        MeConsultaCreateRequest request = new MeConsultaCreateRequest(1L, horario, TipoConsulta.PRESENCIAL, null);

        Consulta resultado = consultaService.criarComoPaciente(1L, request);

        assertThat(resultado.getValor()).isEqualByComparingTo("150.00");
    }

    @Test
    void naoDeveCobrarValorQuandoInformaConvenio() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.existsByProfissionalIdAndDataHoraAndStatusNot(any(), any(), any())).thenReturn(false);
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        MeConsultaCreateRequest request = new MeConsultaCreateRequest(1L, horario, TipoConsulta.PRESENCIAL, "Unimed");

        Consulta resultado = consultaService.criarComoPaciente(1L, request);

        assertThat(resultado.getValor()).isNull();
    }

    // --- isolamento por dono (404, não 403) ---

    @Test
    void naoDevePermitirProfissionalVerConsultaDeOutroProfissional() {
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.buscarPorId(1L, 2L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Consulta não encontrada.");
    }

    @Test
    void naoDevePermitirPacienteVerConsultaDeOutroPaciente() {
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.buscarPorIdEPaciente(1L, 2L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Consulta não encontrada.");
    }

    // --- cancelar / remarcar: só em status editável ---

    @Test
    void naoDevePermitirCancelarConsultaJaRealizada() {
        consulta.setStatus(StatusConsulta.REALIZADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.cancelarComoPaciente(1L, 1L))
                .isInstanceOf(EstadoInvalidoException.class);

        verify(consultaRepository, never()).save(any());
    }

    @Test
    void naoDevePermitirCancelarConsultaJaCancelada() {
        consulta.setStatus(StatusConsulta.CANCELADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.cancelarComoPaciente(1L, 1L))
                .isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void devePermitirCancelarConsultaAgendada() {
        consulta.setStatus(StatusConsulta.AGENDADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        Consulta resultado = consultaService.cancelarComoPaciente(1L, 1L);

        assertThat(resultado.getStatus()).isEqualTo(StatusConsulta.CANCELADA);
    }

    @Test
    void naoDevePermitirRemarcarParaHorarioJaOcupado() {
        consulta.setStatus(StatusConsulta.AGENDADA);
        LocalDateTime novoHorario = horario.plusHours(1);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(consultaRepository.existsByProfissionalIdAndDataHoraAndStatusNotAndIdNot(1L, novoHorario, StatusConsulta.CANCELADA, 1L))
                .thenReturn(true);

        assertThatThrownBy(() -> consultaService.remarcarComoPaciente(1L, 1L, novoHorario))
                .isInstanceOf(HorarioIndisponivelException.class);

        verify(consultaRepository, never()).save(any());
    }

    @Test
    void devePermitirRemarcarParaHorarioLivre() {
        consulta.setStatus(StatusConsulta.AGENDADA);
        LocalDateTime novoHorario = horario.plusHours(1);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(consultaRepository.existsByProfissionalIdAndDataHoraAndStatusNotAndIdNot(1L, novoHorario, StatusConsulta.CANCELADA, 1L))
                .thenReturn(false);
        when(consultaRepository.save(any(Consulta.class))).thenAnswer(inv -> inv.getArgument(0));

        Consulta resultado = consultaService.remarcarComoPaciente(1L, 1L, novoHorario);

        assertThat(resultado.getDataHora()).isEqualTo(novoHorario);
        assertThat(resultado.isFoiRemarcada()).isTrue();
    }

    @Test
    void naoDevePermitirRemarcarConsultaJaRealizada() {
        consulta.setStatus(StatusConsulta.REALIZADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));

        assertThatThrownBy(() -> consultaService.remarcarComoPaciente(1L, 1L, horario.plusHours(1)))
                .isInstanceOf(EstadoInvalidoException.class);
    }
}
