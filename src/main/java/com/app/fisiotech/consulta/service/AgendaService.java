package com.app.fisiotech.consulta.service;
import com.app.fisiotech.consulta.dto.AgendaIndicadoresResponse;
import com.app.fisiotech.consulta.entity.*;
import com.app.fisiotech.consulta.repository.ConsultaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor
public class AgendaService {
 private final ConsultaRepository consultas;
 @Transactional(readOnly=true)
 public List<Consulta> buscar(Long profissionalId,LocalDateTime inicio,LocalDateTime fim,Long pacienteId,StatusConsulta status,TipoConsulta tipo){
  if(fim.isBefore(inicio)) throw new com.app.fisiotech.exception.EstadoInvalidoException("Período inválido.");
  return consultas.buscarAgenda(profissionalId,inicio,fim,pacienteId,status,tipo);
 }
 @Transactional(readOnly=true)
 public AgendaIndicadoresResponse indicadores(Long profissionalId,LocalDateTime inicio,LocalDateTime fim){
  var itens=buscar(profissionalId,inicio,fim,null,null,null);
  Map<String,Long> porStatus=new LinkedHashMap<>(); for(var s:StatusConsulta.values()) porStatus.put(s.name(),0L);
  itens.forEach(c->porStatus.compute(c.getStatus().name(),(k,v)->v+1));
  long pacientes=itens.stream().filter(c->c.getStatus()==StatusConsulta.REALIZADA).map(c->c.getPaciente().getId()).distinct().count();
  long canceladas=porStatus.get(StatusConsulta.CANCELADA.name());
  double taxa=itens.isEmpty()?0.0:(canceladas*100.0/itens.size());
  return new AgendaIndicadoresResponse(itens.size(),pacientes,Math.round(taxa*100.0)/100.0,porStatus);
 }
}
