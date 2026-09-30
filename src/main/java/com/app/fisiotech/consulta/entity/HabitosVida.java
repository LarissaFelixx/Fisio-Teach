package com.app.fisiotech.consulta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class HabitosVida {

    @Column(name = "hv_atividade_fisica", length = 2000)
    private String atividadeFisica;

    @Column(name = "hv_rotina_trabalho", length = 2000)
    private String rotinaTrabalho;

    @Column(name = "hv_tabagismo")
    private Boolean tabagismo;

    @Column(name = "hv_consumo_alcool")
    private Boolean consumoAlcool;
}
