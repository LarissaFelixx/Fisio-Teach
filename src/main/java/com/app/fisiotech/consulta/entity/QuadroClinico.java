package com.app.fisiotech.consulta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class QuadroClinico {

    @Column(name = "qc_queixa_principal", length = 2000)
    private String queixaPrincipal;

    @Column(name = "qc_historia_doenca_atual", length = 2000)
    private String historiaDoencaAtual;

    @Column(name = "qc_historico_saude", length = 255)
    private String historicoSaude;

    @Column(name = "qc_cirurgias")
    private Boolean cirurgias;

    @Column(name = "qc_cirurgias_descricao", length = 2000)
    private String cirurgiasDescricao;

    @Column(name = "qc_lesoes_anteriores")
    private Boolean lesoesAnteriores;

    @Column(name = "qc_lesoes_anteriores_descricao", length = 2000)
    private String lesoesAnterioresDescricao;

    @Column(name = "qc_medicamentos", length = 2000)
    private String medicamentos;
}
