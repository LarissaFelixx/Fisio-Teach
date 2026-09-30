package com.app.fisiotech.paciente.repository;

import com.app.fisiotech.paciente.entity.Paciente;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<Paciente> findByEmail(String email);

    List<Paciente> findByProfissionalId(Long profissionalId, Sort sort);

}
