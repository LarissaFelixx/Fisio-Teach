package com.app.fisiotech.profissional.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProfissionalUpdateRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        @Size(max = 120, message = "O email deve ter no máximo 120 caracteres")
        String email,

        @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres")
        String senha,

        @NotBlank(message = "O registro profissional é obrigatório")
        @Size(max = 20, message = "O registro profissional deve ter no máximo 20 caracteres")
        String registroProfissional,

        @NotBlank(message = "A especialidade é obrigatória")
        @Size(max = 120, message = "A especialidade deve ter no máximo 120 caracteres")
        String especialidade,

        @DecimalMin(value = "0.0", inclusive = true, message = "O valor não pode ser negativo")
        BigDecimal valorConsultaParticular,

        List<String> conveniosAceitos,

        String foto,

        LocalDate dataNascimento,

        String sexo,

        String telefone
) {
}
