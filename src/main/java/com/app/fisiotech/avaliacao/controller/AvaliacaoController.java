package com.app.fisiotech.avaliacao.controller;

import com.app.fisiotech.auth.security.AuthenticatedUser;
import com.app.fisiotech.avaliacao.dto.AvaliacaoCreateRequest;
import com.app.fisiotech.avaliacao.dto.AvaliacaoResponse;
import com.app.fisiotech.avaliacao.entity.Avaliacao;
import com.app.fisiotech.avaliacao.service.AvaliacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @PostMapping
    public ResponseEntity<Void> criar(
            @Valid @RequestBody AvaliacaoCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        Avaliacao avaliacaoCriada = avaliacaoService.criar(request, usuarioLogado.getId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(avaliacaoCriada.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }


    @GetMapping("/consulta/{consultaId}")
    public ResponseEntity<AvaliacaoResponse> buscarPorConsulta(
            @PathVariable Long consultaId,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        Avaliacao avaliacao = avaliacaoService.buscarPorConsulta(consultaId, usuarioLogado.getId());
        return ResponseEntity.ok(AvaliacaoResponse.fromEntity(avaliacao));
    }

}
