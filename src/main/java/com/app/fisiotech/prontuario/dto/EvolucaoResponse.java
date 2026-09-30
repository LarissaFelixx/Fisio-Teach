package com.app.fisiotech.prontuario.dto;
import com.app.fisiotech.prontuario.entity.EvolucaoClinica;
import java.time.LocalDateTime;
public record EvolucaoResponse(Long id, Long pacienteId, Long profissionalId, String profissionalNome, Long consultaId,
 String observacoes, String procedimentos, String respostaPaciente, String conduta, boolean visivelPaciente, LocalDateTime dataRegistro) {
 public static EvolucaoResponse from(EvolucaoClinica e) { return new EvolucaoResponse(e.getId(),e.getPaciente().getId(),e.getProfissional().getId(),e.getProfissional().getNome(),e.getConsulta()==null?null:e.getConsulta().getId(),e.getObservacoes(),e.getProcedimentos(),e.getRespostaPaciente(),e.getConduta(),e.isVisivelPaciente(),e.getDataRegistro()); }
}
