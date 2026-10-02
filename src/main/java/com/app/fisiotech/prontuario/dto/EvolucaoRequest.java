package com.app.fisiotech.prontuario.dto;
import jakarta.validation.constraints.*;
public record EvolucaoRequest(Long consultaId, @NotBlank @Size(max=4000) String observacoes,
        @Size(max=4000) String procedimentos, @Size(max=2000) String respostaPaciente,
        @Size(max=2000) String conduta, boolean visivelPaciente) {}
