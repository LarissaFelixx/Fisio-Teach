package com.app.fisiotech.prontuario.dto;
import com.app.fisiotech.prontuario.entity.*;
import java.time.*;
public record PlanoResponse(Long id,Long pacienteId,Long profissionalId,String profissionalNome,Integer revisao,String objetivos,String condutas,LocalDate dataInicio,LocalDate dataFimPrevista,StatusPlano status,boolean visivelPaciente,LocalDateTime dataCriacao) {
 public static PlanoResponse from(PlanoTerapeutico p) { return new PlanoResponse(p.getId(),p.getPaciente().getId(),p.getProfissional().getId(),p.getProfissional().getNome(),p.getRevisao(),p.getObjetivos(),p.getCondutas(),p.getDataInicio(),p.getDataFimPrevista(),p.getStatus(),p.isVisivelPaciente(),p.getDataCriacao()); }
}
