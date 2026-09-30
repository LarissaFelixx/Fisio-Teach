package com.app.fisiotech.consulta.repository;

import com.app.fisiotech.consulta.entity.Consulta;
import com.app.fisiotech.consulta.entity.StatusConsulta;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByProfissionalId(Long profissionalId, Sort sort);

    List<Consulta> findByPacienteIdAndProfissionalId(Long pacienteId, Long profissionalId, Sort sort);

    List<Consulta> findByPaciente_Id(Long pacienteId, Sort sort);

    boolean existsByProfissionalIdAndDataHoraAndStatusNot(Long profissionalId, LocalDateTime dataHora, StatusConsulta status);

    boolean existsByProfissionalIdAndDataHoraAndStatusNotAndIdNot(Long profissionalId, LocalDateTime dataHora, StatusConsulta status, Long id);

    List<Consulta> findByProfissionalIdAndDataHoraBetweenAndStatusNot(Long profissionalId, LocalDateTime inicio, LocalDateTime fim, StatusConsulta status);

    boolean existsByPacienteIdAndProfissionalId(Long pacienteId, Long profissionalId);

}
