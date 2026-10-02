package com.app.fisiotech.consulta.controller;
import com.app.fisiotech.auth.security.AuthenticatedUser;
import com.app.fisiotech.consulta.dto.*;
import com.app.fisiotech.consulta.entity.*;
import com.app.fisiotech.consulta.service.AgendaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime; import java.util.List;

@RestController @RequestMapping("/agenda") @RequiredArgsConstructor
public class AgendaController {
 private final AgendaService service;
 @GetMapping public List<ConsultaResponse> agenda(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,@RequestParam(required=false) Long pacienteId,@RequestParam(required=false) StatusConsulta status,@RequestParam(required=false) TipoConsulta tipo,@AuthenticationPrincipal AuthenticatedUser u){return service.buscar(u.getId(),inicio,fim,pacienteId,status,tipo).stream().map(ConsultaResponse::fromEntity).toList();}
 @GetMapping("/indicadores") public AgendaIndicadoresResponse indicadores(@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,@RequestParam @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,@AuthenticationPrincipal AuthenticatedUser u){return service.indicadores(u.getId(),inicio,fim);}
}
