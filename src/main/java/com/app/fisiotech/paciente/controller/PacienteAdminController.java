package com.app.fisiotech.paciente.controller;

import com.app.fisiotech.paciente.dto.PacienteAdminUpdateRequest;
import com.app.fisiotech.paciente.dto.PacienteResponse;
import com.app.fisiotech.paciente.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import com.app.fisiotech.common.dto.PageResponse;
import com.app.fisiotech.common.dto.PageRequestFactory;

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

    @GetMapping("/paginado")
    public ResponseEntity<PageResponse<PacienteResponse>> buscarPaginado(
            @RequestParam(required = false) String filtro,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        var pageable = PageRequestFactory.create(page, size, sort, direction, Set.of("id", "nome", "email"));
        return ResponseEntity.ok(PageResponse.from(pacienteService.buscarPaginadoAdmin(filtro, pageable), PacienteResponse::fromEntity));
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
