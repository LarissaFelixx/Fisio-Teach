package com.app.fisiotech.consulta.dto;
import java.util.Map;
public record AgendaIndicadoresResponse(long totalConsultas, long pacientesAtendidos,
        double taxaCancelamento, Map<String,Long> consultasPorStatus) {}
