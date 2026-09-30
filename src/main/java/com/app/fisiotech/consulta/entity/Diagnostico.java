package com.app.fisiotech.consulta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Diagnostico {

    @Column(name = "dx_plano_tratamento", length = 2000)
    private String planoTratamento;

    @Column(name = "dx_objetivos_tratamento", length = 2000)
    private String objetivosTratamento;
}
