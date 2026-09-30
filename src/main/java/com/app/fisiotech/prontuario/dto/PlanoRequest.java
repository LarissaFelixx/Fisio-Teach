package com.app.fisiotech.prontuario.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record PlanoRequest(@NotBlank @Size(max=4000) String objetivos, @NotBlank @Size(max=4000) String condutas,
        @NotNull LocalDate dataInicio, LocalDate dataFimPrevista, boolean visivelPaciente) {}
