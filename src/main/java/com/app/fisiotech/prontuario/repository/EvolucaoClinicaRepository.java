package com.app.fisiotech.prontuario.repository;
import com.app.fisiotech.prontuario.entity.EvolucaoClinica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EvolucaoClinicaRepository extends JpaRepository<EvolucaoClinica,Long> {
 List<EvolucaoClinica> findByPacienteIdAndProfissionalIdOrderByDataRegistroDesc(Long pacienteId,Long profissionalId);
 List<EvolucaoClinica> findByPacienteIdAndVisivelPacienteTrueOrderByDataRegistroDesc(Long pacienteId);
}
