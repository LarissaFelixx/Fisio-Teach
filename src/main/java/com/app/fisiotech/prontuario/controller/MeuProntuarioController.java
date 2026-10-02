package com.app.fisiotech.prontuario.controller;

import com.app.fisiotech.auth.security.AuthenticatedUser;
import com.app.fisiotech.prontuario.dto.*;
import com.app.fisiotech.prontuario.service.ProntuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/me/prontuario") @RequiredArgsConstructor
public class MeuProntuarioController {
 private final ProntuarioService service;
 @GetMapping("/evolucoes") public List<EvolucaoResponse> evolucoes(@AuthenticationPrincipal AuthenticatedUser u){return service.evolucoesVisiveis(u.getId()).stream().map(EvolucaoResponse::from).toList();}
 @GetMapping("/planos") public List<PlanoResponse> planos(@AuthenticationPrincipal AuthenticatedUser u){return service.planosVisiveis(u.getId()).stream().map(PlanoResponse::from).toList();}
}
