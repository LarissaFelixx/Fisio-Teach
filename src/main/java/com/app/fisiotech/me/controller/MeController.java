package com.app.fisiotech.me.controller;

import com.app.fisiotech.auth.security.AuthenticatedUser;
import com.app.fisiotech.avaliacao.dto.AvaliacaoCreateRequest;
import com.app.fisiotech.avaliacao.dto.AvaliacaoResponse;
import com.app.fisiotech.avaliacao.service.AvaliacaoService;
import com.app.fisiotech.consulta.dto.ConsultaResponse;
import com.app.fisiotech.consulta.dto.DisponibilidadeResponse;
import com.app.fisiotech.consulta.dto.MeConsultaCreateRequest;
import com.app.fisiotech.consulta.dto.RemarcarConsultaRequest;
import com.app.fisiotech.consulta.entity.Consulta;
import com.app.fisiotech.consulta.service.ConsultaService;
import com.app.fisiotech.consulta.service.DisponibilidadeService;
import com.app.fisiotech.me.dto.MinhaMensagemCreateRequest;
import com.app.fisiotech.mensagem.dto.MensagemResponse;
import com.app.fisiotech.mensagem.dto.MinhaConversaResponse;
import com.app.fisiotech.mensagem.service.MensagemService;
import com.app.fisiotech.auth.dto.AlterarSenhaRequest;
import com.app.fisiotech.paciente.dto.MePerfilUpdateRequest;
import com.app.fisiotech.paciente.dto.PacienteResponse;
import com.app.fisiotech.paciente.service.PacienteService;
import com.app.fisiotech.profissional.dto.ProfissionalPublicoResponse;
import com.app.fisiotech.profissional.service.ProfissionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class MeController {

    private final PacienteService pacienteService;
    private final ConsultaService consultaService;
    private final MensagemService mensagemService;
    private final AvaliacaoService avaliacaoService;
    private final ProfissionalService profissionalService;
    private final DisponibilidadeService disponibilidadeService;

    @GetMapping
    public ResponseEntity<PacienteResponse> perfil(@AuthenticationPrincipal AuthenticatedUser usuarioLogado) {
        return ResponseEntity.ok(PacienteResponse.fromEntity(pacienteService.buscarProprioPerfil(usuarioLogado.getId())));
    }

    @PutMapping
    public ResponseEntity<Void> atualizarPerfil(
            @Valid @RequestBody MePerfilUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        pacienteService.atualizarPerfilProprio(usuarioLogado.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> alterarSenha(
            @Valid @RequestBody AlterarSenhaRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        pacienteService.alterarSenha(usuarioLogado.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profissionais")
    public ResponseEntity<List<ProfissionalPublicoResponse>> buscarProfissionais(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String especialidade
    ) {
        List<ProfissionalPublicoResponse> response = profissionalService.buscarPublico(nome, especialidade)
                .stream()
                .map(ProfissionalPublicoResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profissionais/{id}")
    public ResponseEntity<ProfissionalPublicoResponse> buscarProfissional(@PathVariable Long id) {
        return ResponseEntity.ok(ProfissionalPublicoResponse.fromEntity(profissionalService.buscarPorId(id)));
    }

    @GetMapping("/profissionais/{id}/disponibilidade")
    public ResponseEntity<DisponibilidadeResponse> buscarDisponibilidade(
            @PathVariable Long id,
            @RequestParam LocalDate data
    ) {
        return ResponseEntity.ok(disponibilidadeService.buscarDisponibilidade(id, data));
    }

    @PostMapping("/consultas")
    public ResponseEntity<ConsultaResponse> marcarConsulta(
            @Valid @RequestBody MeConsultaCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        Consulta consultaCriada = consultaService.criarComoPaciente(usuarioLogado.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ConsultaResponse.fromEntity(consultaCriada));
    }

    @GetMapping("/consultas")
    public ResponseEntity<List<ConsultaResponse>> minhasConsultas(@AuthenticationPrincipal AuthenticatedUser usuarioLogado) {
        List<ConsultaResponse> response = consultaService.listarDoPacienteLogado(usuarioLogado.getId())
                .stream()
                .map(ConsultaResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/consultas/{id}")
    public ResponseEntity<ConsultaResponse> minhaConsulta(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        return ResponseEntity.ok(ConsultaResponse.fromEntity(consultaService.buscarPorIdEPaciente(id, usuarioLogado.getId())));
    }

    @PutMapping("/consultas/{id}/cancelar")
    public ResponseEntity<Void> cancelarConsulta(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        consultaService.cancelarComoPaciente(id, usuarioLogado.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/consultas/{id}/remarcar")
    public ResponseEntity<ConsultaResponse> remarcarConsulta(
            @PathVariable Long id,
            @Valid @RequestBody RemarcarConsultaRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        Consulta consulta = consultaService.remarcarComoPaciente(id, usuarioLogado.getId(), request.novaDataHora());
        return ResponseEntity.ok(ConsultaResponse.fromEntity(consulta));
    }

    @GetMapping("/mensagens/caixa-entrada")
    public ResponseEntity<List<MinhaConversaResponse>> minhasConversas(@AuthenticationPrincipal AuthenticatedUser usuarioLogado) {
        return ResponseEntity.ok(mensagemService.listarConversasDoPaciente(usuarioLogado.getId()));
    }

    @GetMapping("/mensagens/{profissionalId}")
    public ResponseEntity<List<MensagemResponse>> minhaConversa(
            @PathVariable Long profissionalId,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        List<MensagemResponse> response = mensagemService.listarConversaDoPaciente(usuarioLogado.getId(), profissionalId)
                .stream()
                .map(MensagemResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/mensagens/{profissionalId}")
    public ResponseEntity<Void> enviarMensagem(
            @PathVariable Long profissionalId,
            @Valid @RequestBody MinhaMensagemCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        mensagemService.enviarComoPaciente(usuarioLogado.getId(), profissionalId, request.conteudo());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/avaliacoes")
    public ResponseEntity<Void> avaliar(
            @Valid @RequestBody AvaliacaoCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        avaliacaoService.criarComoPaciente(request, usuarioLogado.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/avaliacoes/consulta/{consultaId}")
    public ResponseEntity<AvaliacaoResponse> minhaAvaliacao(
            @PathVariable Long consultaId,
            @AuthenticationPrincipal AuthenticatedUser usuarioLogado
    ) {
        return ResponseEntity.ok(AvaliacaoResponse.fromEntity(avaliacaoService.buscarPorConsultaEPaciente(consultaId, usuarioLogado.getId())));
    }

}
