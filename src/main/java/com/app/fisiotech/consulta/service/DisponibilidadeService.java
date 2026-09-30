package com.app.fisiotech.consulta.service;

import com.app.fisiotech.consulta.dto.DisponibilidadeResponse;
import com.app.fisiotech.consulta.dto.DisponibilidadeResponse.SlotDisponibilidade;
import com.app.fisiotech.consulta.entity.StatusConsulta;
import com.app.fisiotech.consulta.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisponibilidadeService {

    private static final LocalTime INICIO_EXPEDIENTE = LocalTime.of(8, 0);
    private static final LocalTime FIM_EXPEDIENTE = LocalTime.of(18, 0);
    private static final int PASSO_MINUTOS = 30;

    private final ConsultaRepository consultaRepository;

    @Transactional(readOnly = true)
    public DisponibilidadeResponse buscarDisponibilidade(Long profissionalId, LocalDate data) {
        Set<LocalTime> horariosOcupados = consultaRepository
                .findByProfissionalIdAndDataHoraBetweenAndStatusNot(
                        profissionalId,
                        data.atStartOfDay(),
                        data.atTime(23, 59, 59),
                        StatusConsulta.CANCELADA
                )
                .stream()
                .map(consulta -> consulta.getDataHora().toLocalTime())
                .collect(Collectors.toSet());

        List<SlotDisponibilidade> horarios = gerarGrade().stream()
                .map(horario -> new SlotDisponibilidade(horario, !horariosOcupados.contains(horario)))
                .toList();

        return new DisponibilidadeResponse(data, horarios);
    }

    private List<LocalTime> gerarGrade() {
        List<LocalTime> grade = new java.util.ArrayList<>();
        LocalTime horario = INICIO_EXPEDIENTE;

        while (horario.isBefore(FIM_EXPEDIENTE)) {
            grade.add(horario);
            horario = horario.plusMinutes(PASSO_MINUTOS);
        }

        return grade;
    }
}
