package com.app.fisiotech.consulta.repository;

import com.app.fisiotech.consulta.entity.Consulta;
import com.app.fisiotech.consulta.entity.StatusConsulta;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import com.app.fisiotech.consulta.entity.TipoConsulta;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByProfissionalId(Long profissionalId, Sort sort);

    List<Consulta> findByPacienteIdAndProfissionalId(Long pacienteId, Long profissionalId, Sort sort);

    List<Consulta> findByPaciente_Id(Long pacienteId, Sort sort);

    boolean existsByProfissionalIdAndDataHoraAndStatusNot(Long profissionalId, LocalDateTime dataHora, StatusConsulta status);

    boolean existsByProfissionalIdAndDataHoraAndStatusNotAndIdNot(Long profissionalId, LocalDateTime dataHora, StatusConsulta status, Long id);

    List<Consulta> findByProfissionalIdAndDataHoraBetweenAndStatusNot(Long profissionalId, LocalDateTime inicio, LocalDateTime fim, StatusConsulta status);

    boolean existsByPacienteIdAndProfissionalId(Long pacienteId, Long profissionalId);

    @Query("""
        select c from Consulta c where c.profissional.id = :profissionalId
        and c.dataHora between :inicio and :fim
        and (:pacienteId is null or c.paciente.id = :pacienteId)
        and (:status is null or c.status = :status)
        and (:tipo is null or c.tipo = :tipo)
        order by c.dataHora asc, c.id asc
        """)
    List<Consulta> buscarAgenda(@Param("profissionalId") Long profissionalId,
            @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim,
            @Param("pacienteId") Long pacienteId, @Param("status") StatusConsulta status,
            @Param("tipo") TipoConsulta tipo);

}
