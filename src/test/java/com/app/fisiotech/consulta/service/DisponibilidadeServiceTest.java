package com.app.fisiotech.consulta.service;

import com.app.fisiotech.consulta.dto.DisponibilidadeResponse;
import com.app.fisiotech.consulta.entity.Consulta;
import com.app.fisiotech.consulta.entity.StatusConsulta;
import com.app.fisiotech.consulta.entity.TipoConsulta;
import com.app.fisiotech.consulta.repository.ConsultaRepository;
import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.profissional.entity.Profissional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisponibilidadeServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @InjectMocks
    private DisponibilidadeService disponibilidadeService;

    @Test
    void deveMarcarHorarioComoIndisponivelQuandoJaExisteConsultaNaqueleHorario() {
        LocalDate data = LocalDate.of(2026, 8, 25);
        Profissional profissional = new Profissional("Ana Souza", "ana@fisiotech.com", "hash", "CREFITO-1", "Ortopedia");
        Paciente paciente = new Paciente("Joao Silva", "joao@paciente.com", "hash", profissional);
        Consulta consultaOcupada = new Consulta(paciente, profissional, data.atTime(10, 0), TipoConsulta.PRESENCIAL, null, null);

        when(consultaRepository.findByProfissionalIdAndDataHoraBetweenAndStatusNot(any(), any(), any(), any()))
                .thenReturn(List.of(consultaOcupada));

        DisponibilidadeResponse resposta = disponibilidadeService.buscarDisponibilidade(1L, data);

        boolean dezHorasDisponivel = resposta.horarios().stream()
                .filter(slot -> slot.horario().equals(LocalTime.of(10, 0)))
                .findFirst()
                .orElseThrow()
                .disponivel();

        assertThat(dezHorasDisponivel).isFalse();
    }

    @Test
    void deveMarcarTodosOsHorariosComoDisponiveisQuandoNaoHaConsultas() {
        LocalDate data = LocalDate.of(2026, 8, 25);
        when(consultaRepository.findByProfissionalIdAndDataHoraBetweenAndStatusNot(any(), any(), any(), any()))
                .thenReturn(List.of());

        DisponibilidadeResponse resposta = disponibilidadeService.buscarDisponibilidade(1L, data);

        assertThat(resposta.horarios()).isNotEmpty();
        assertThat(resposta.horarios()).allMatch(DisponibilidadeResponse.SlotDisponibilidade::disponivel);
    }

    @Test
    void deveGerarGradeDas8As18EmPassosDe30Minutos() {
        LocalDate data = LocalDate.of(2026, 8, 25);
        when(consultaRepository.findByProfissionalIdAndDataHoraBetweenAndStatusNot(any(), any(), any(), any()))
                .thenReturn(List.of());

        DisponibilidadeResponse resposta = disponibilidadeService.buscarDisponibilidade(1L, data);

        assertThat(resposta.horarios()).hasSize(20); // 08:00 a 17:30, passo de 30min
        assertThat(resposta.horarios().get(0).horario()).isEqualTo(LocalTime.of(8, 0));
        assertThat(resposta.horarios().get(resposta.horarios().size() - 1).horario()).isEqualTo(LocalTime.of(17, 30));
    }
}
