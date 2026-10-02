package com.app.fisiotech.profissional.repository;

import com.app.fisiotech.profissional.entity.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByRegistroProfissional(String registroProfissional);

    boolean existsByRegistroProfissionalAndIdNot(String registroProfissional, Long id);

    Optional<Profissional> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Profissional> findWithLockById(Long id);

    @Query("select p from Profissional p where (:nome is null or lower(p.nome) like lower(concat('%', :nome, '%'))) " +
            "and (:especialidade is null or lower(p.especialidade) like lower(concat('%', :especialidade, '%')))")
    Page<Profissional> buscar(String nome, String especialidade, Pageable pageable);

}
