package com.app.fisiotech.prontuario.controller;

import com.app.fisiotech.auth.security.AuthenticatedUser;
import com.app.fisiotech.prontuario.dto.*;
import com.app.fisiotech.prontuario.service.ProntuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/prontuario/pacientes/{pacienteId}") @RequiredArgsConstructor
public class ProntuarioController {
 private final ProntuarioService service;
 @PostMapping("/evolucoes") public ResponseEntity<EvolucaoResponse> evoluir(@PathVariable Long pacienteId,@Valid @RequestBody EvolucaoRequest r,@AuthenticationPrincipal AuthenticatedUser u){return ResponseEntity.status(201).body(EvolucaoResponse.from(service.registrarEvolucao(pacienteId,u.getId(),r)));}
 @GetMapping("/evolucoes") public List<EvolucaoResponse> evolucoes(@PathVariable Long pacienteId,@AuthenticationPrincipal AuthenticatedUser u){return service.evolucoes(pacienteId,u.getId()).stream().map(EvolucaoResponse::from).toList();}
 @PostMapping("/planos") public ResponseEntity<PlanoResponse> plano(@PathVariable Long pacienteId,@Valid @RequestBody PlanoRequest r,@AuthenticationPrincipal AuthenticatedUser u){return ResponseEntity.status(201).body(PlanoResponse.from(service.revisarPlano(pacienteId,u.getId(),r)));}
 @GetMapping("/planos") public List<PlanoResponse> planos(@PathVariable Long pacienteId,@AuthenticationPrincipal AuthenticatedUser u){return service.planos(pacienteId,u.getId()).stream().map(PlanoResponse::from).toList();}
}
