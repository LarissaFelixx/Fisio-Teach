package com.app.fisiotech.avaliacao.repository;

import com.app.fisiotech.avaliacao.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    boolean existsByConsultaId(Long consultaId);

    Optional<Avaliacao> findByConsultaId(Long consultaId);

}
