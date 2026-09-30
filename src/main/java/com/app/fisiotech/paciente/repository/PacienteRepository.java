package com.app.fisiotech.paciente.repository;

import com.app.fisiotech.paciente.entity.Paciente;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<Paciente> findByEmail(String email);

    List<Paciente> findByProfissionalId(Long profissionalId, Sort sort);

    @Query("select p from Paciente p where p.profissional.id = :profissionalId and " +
            "(:filtro is null or lower(p.nome) like lower(concat('%', :filtro, '%')) or lower(p.email) like lower(concat('%', :filtro, '%')))")
    Page<Paciente> buscarDoProfissional(Long profissionalId, String filtro, Pageable pageable);

    @Query("select p from Paciente p where :filtro is null or lower(p.nome) like lower(concat('%', :filtro, '%')) " +
            "or lower(p.email) like lower(concat('%', :filtro, '%'))")
    Page<Paciente> buscarTodos(String filtro, Pageable pageable);

}
