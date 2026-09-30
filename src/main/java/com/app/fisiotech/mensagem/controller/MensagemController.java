package com.app.fisiotech.mensagem.controller;

import com.app.fisiotech.auth.security.AuthenticatedUser;
import com.app.fisiotech.mensagem.dto.CaixaEntradaItemResponse;
import com.app.fisiotech.mensagem.dto.MensagemCreateRequest;
import com.app.fisiotech.mensagem.dto.MensagemResponse;
import com.app.fisiotech.mensagem.entity.Mensagem;
import com.app.fisiotech.mensagem.service.MensagemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/mensagens")
@RequiredArgsConstructor
public class MensagemController {

    private final MensagemService mensagemService;

    @PostMapping
    public ResponseEntity<Void> enviar(
            @Valid @RequestBody MensagemCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        Mensagem mensagemEnviada = mensagemService.enviar(request, usuarioLogado.getId());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(mensagemEnviada.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }


    @GetMapping
    public ResponseEntity<List<MensagemResponse>> listarPorPaciente(
            @RequestParam Long pacienteId,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        List<MensagemResponse> response = mensagemService.listarPorPaciente(pacienteId, usuarioLogado.getId())
                .stream()
                .map(MensagemResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/caixa-entrada")
    public ResponseEntity<List<CaixaEntradaItemResponse>> caixaEntrada(@AuthenticationPrincipal AuthenticatedUser usuarioLogado) {
        return ResponseEntity.ok(mensagemService.listarCaixaEntrada(usuarioLogado.getId()));
    }

}
