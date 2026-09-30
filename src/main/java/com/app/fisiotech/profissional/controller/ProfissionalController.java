package com.app.fisiotech.profissional.controller;

import com.app.fisiotech.auth.dto.AlterarSenhaRequest;
import com.app.fisiotech.auth.security.AuthenticatedUser;
import com.app.fisiotech.profissional.dto.ProfissionalCreateRequest;
import com.app.fisiotech.profissional.dto.ProfissionalResponse;
import com.app.fisiotech.profissional.dto.ProfissionalUpdateRequest;
import com.app.fisiotech.profissional.entity.Profissional;
import com.app.fisiotech.profissional.service.ProfissionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    @PostMapping
    public ResponseEntity<Void> criar(@Valid @RequestBody ProfissionalCreateRequest request) {
        Profissional profissionalCriado = profissionalService.criar(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(profissionalCriado.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }


    @GetMapping
    public ResponseEntity<List<ProfissionalResponse>> listarTodos() {
        List<ProfissionalResponse> response = profissionalService.listarTodos()
                .stream()
                .map(ProfissionalResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalResponse> buscarPorId(@PathVariable Long id) {
        Profissional profissional = profissionalService.buscarPorId(id);
        return ResponseEntity.ok(ProfissionalResponse.fromEntity(profissional));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizarProfissional(
            @PathVariable Long id,
            @Valid @RequestBody ProfissionalUpdateRequest request
    ) {
        profissionalService.atualizar(id, request);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPorId(@PathVariable Long id) {
        profissionalService.deletar(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/me/senha")
    public ResponseEntity<Void> alterarPropriaSenha(
            @Valid @RequestBody AlterarSenhaRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        profissionalService.alterarSenha(usuarioLogado.getId(), request);
        return ResponseEntity.noContent().build();
    }

}
