package com.app.fisiotech.prontuario.repository;
import com.app.fisiotech.prontuario.entity.PlanoTerapeutico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface PlanoTerapeuticoRepository extends JpaRepository<PlanoTerapeutico,Long> {
 List<PlanoTerapeutico> findByPacienteIdAndProfissionalIdOrderByRevisaoDesc(Long pacienteId,Long profissionalId);
 List<PlanoTerapeutico> findByPacienteIdAndVisivelPacienteTrueOrderByRevisaoDesc(Long pacienteId);
 Optional<PlanoTerapeutico> findFirstByPacienteIdAndProfissionalIdOrderByRevisaoDesc(Long pacienteId,Long profissionalId);
}
