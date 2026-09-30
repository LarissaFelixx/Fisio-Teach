package com.app.fisiotech.paciente.controller;

import com.app.fisiotech.auth.security.AuthenticatedUser;
import com.app.fisiotech.paciente.dto.PacienteCreateRequest;
import com.app.fisiotech.paciente.dto.PacienteResponse;
import com.app.fisiotech.paciente.dto.PacienteUpdateRequest;
import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.paciente.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @PostMapping("/cadastro")
    public ResponseEntity<Void> cadastrarPublico(@Valid @RequestBody PacienteCreateRequest request) {
        Paciente pacienteCriado = pacienteService.cadastrarPublico(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/pacientes/{id}")
                .buildAndExpand(pacienteCriado.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }


    @PostMapping
    public ResponseEntity<Void> criar(
            @Valid @RequestBody PacienteCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        Paciente pacienteCriado = pacienteService.criar(request, usuarioLogado.getId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(pacienteCriado.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }


    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listarTodos(@AuthenticationPrincipal AuthenticatedUser usuarioLogado){
        List<PacienteResponse> response = pacienteService.listarTodos(usuarioLogado.getId())
                .stream()
                .map(PacienteResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ){
        Paciente paciente = pacienteService.buscarPorId(id, usuarioLogado.getId());
        return ResponseEntity.ok(PacienteResponse.fromEntity(paciente));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarPaciente(
            @PathVariable Long id,
            @Valid @RequestBody PacienteUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
            ){
        pacienteService.atualizar(id, request, usuarioLogado.getId());
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ){
        pacienteService.deletar(id, usuarioLogado.getId());
        return ResponseEntity.noContent().build();
    }

}
