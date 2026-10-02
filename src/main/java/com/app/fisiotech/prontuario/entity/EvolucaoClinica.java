package com.app.fisiotech.prontuario.entity;

import com.app.fisiotech.consulta.entity.Consulta;
import com.app.fisiotech.paciente.entity.Paciente;
import com.app.fisiotech.profissional.entity.Profissional;
import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

@Entity @Table(name="evolucoes_clinicas") @Getter
public class EvolucaoClinica {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="paciente_id") private Paciente paciente;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="profissional_id") private Profissional profissional;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="consulta_id") private Consulta consulta;
    @Column(nullable=false,length=4000) private String observacoes;
    @Column(length=4000) private String procedimentos;
    @Column(name="resposta_paciente",length=2000) private String respostaPaciente;
    @Column(length=2000) private String conduta;
    @Column(name="visivel_paciente",nullable=false) private boolean visivelPaciente;
    @Column(name="data_registro",nullable=false,updatable=false) private LocalDateTime dataRegistro;
    protected EvolucaoClinica() {}
    public EvolucaoClinica(Paciente p, Profissional prof, Consulta c, String obs, String proc, String resposta, String conduta, boolean visivel) {
        paciente=p; profissional=prof; consulta=c; observacoes=obs; procedimentos=proc;
        respostaPaciente=resposta; this.conduta=conduta; visivelPaciente=visivel; dataRegistro=LocalDateTime.now();
    }
}
