package com.app.fisiotech.auth.repository;

import com.app.fisiotech.auth.entity.CodigoRecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodigoRecuperacaoSenhaRepository extends JpaRepository<CodigoRecuperacaoSenha, Long> {

    Optional<CodigoRecuperacaoSenha> findFirstByEmailOrderByIdDesc(String email);

    void deleteByEmail(String email);

}
