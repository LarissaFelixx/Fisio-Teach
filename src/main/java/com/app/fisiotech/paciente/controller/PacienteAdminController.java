package com.app.fisiotech.paciente.controller;

import com.app.fisiotech.paciente.dto.PacienteAdminUpdateRequest;
import com.app.fisiotech.paciente.dto.PacienteResponse;
import com.app.fisiotech.paciente.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/pacientes")
@RequiredArgsConstructor
public class PacienteAdminController {

    private final PacienteService pacienteService;

    @GetMapping
    public ResponseEntity<List<PacienteResponse>> listarTodos() {
        List<PacienteResponse> response = pacienteService.listarTodosAdmin()
                .stream()
                .map(PacienteResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(PacienteResponse.fromEntity(pacienteService.buscarPorIdAdmin(id)));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PacienteAdminUpdateRequest request
    ) {
        pacienteService.atualizarAdmin(id, request);
        return ResponseEntity.noContent().build();
    }

}
