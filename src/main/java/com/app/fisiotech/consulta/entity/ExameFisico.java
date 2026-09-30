package com.app.fisiotech.consulta.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class ExameFisico {

    @Column(name = "ef_postura", length = 2000)
    private String postura;

    @Column(name = "ef_amplitude_movimento", length = 2000)
    private String amplitudeMovimento;

    @Column(name = "ef_palpacao", length = 2000)
    private String palpacao;

    @Column(name = "ef_forca_muscular", length = 2000)
    private String forcaMuscular;
}
