package com.app.fisiotech.consulta.controller;

import com.app.fisiotech.auth.security.AuthenticatedUser;
import com.app.fisiotech.consulta.dto.ConsultaCreateRequest;
import com.app.fisiotech.consulta.dto.ConsultaResponse;
import com.app.fisiotech.consulta.dto.ConsultaUpdateRequest;
import com.app.fisiotech.consulta.entity.Consulta;
import com.app.fisiotech.consulta.service.ConsultaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/consultas")
@RequiredArgsConstructor
public class ConsultaController {

    private final ConsultaService consultaService;

    @PostMapping
    public ResponseEntity<Void> criar(
            @Valid @RequestBody ConsultaCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        Consulta consultaCriada = consultaService.criar(request, usuarioLogado.getId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(consultaCriada.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }


    @GetMapping
    public ResponseEntity<List<ConsultaResponse>> listarTodos(
            @RequestParam(required = false) Long pacienteId,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        List<ConsultaResponse> response = consultaService.listarTodos(usuarioLogado.getId(), pacienteId)
                .stream()
                .map(ConsultaResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ConsultaResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        Consulta consulta = consultaService.buscarPorId(id, usuarioLogado.getId());
        return ResponseEntity.ok(ConsultaResponse.fromEntity(consulta));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ConsultaUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        consultaService.atualizar(id, request, usuarioLogado.getId());
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        consultaService.deletar(id, usuarioLogado.getId());
        return ResponseEntity.noContent().build();
    }

}
