package com.app.fisiotech.profissional.repository;

import com.app.fisiotech.profissional.entity.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByRegistroProfissional(String registroProfissional);

    boolean existsByRegistroProfissionalAndIdNot(String registroProfissional, Long id);

    Optional<Profissional> findByEmail(String email);

}
