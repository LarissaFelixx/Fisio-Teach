package com.app.fisiotech.consulta.dto;

import com.app.fisiotech.consulta.entity.QuadroClinico;
import jakarta.validation.constraints.Size;

public record QuadroClinicoDto(
        @Size(max = 2000, message = "A queixa principal deve ter no máximo 2000 caracteres")
        String queixaPrincipal,

        @Size(max = 2000, message = "A história da doença atual deve ter no máximo 2000 caracteres")
        String historiaDoencaAtual,

        @Size(max = 255, message = "O histórico de saúde deve ter no máximo 255 caracteres")
        String historicoSaude,

        Boolean cirurgias,

        @Size(max = 2000, message = "A descrição das cirurgias deve ter no máximo 2000 caracteres")
        String cirurgiasDescricao,

        Boolean lesoesAnteriores,

        @Size(max = 2000, message = "A descrição das lesões anteriores deve ter no máximo 2000 caracteres")
        String lesoesAnterioresDescricao,

        @Size(max = 2000, message = "Os medicamentos devem ter no máximo 2000 caracteres")
        String medicamentos
) {

    public static QuadroClinicoDto fromEntity(QuadroClinico quadroClinico) {
        return new QuadroClinicoDto(
                quadroClinico.getQueixaPrincipal(),
                quadroClinico.getHistoriaDoencaAtual(),
                quadroClinico.getHistoricoSaude(),
                quadroClinico.getCirurgias(),
                quadroClinico.getCirurgiasDescricao(),
                quadroClinico.getLesoesAnteriores(),
                quadroClinico.getLesoesAnterioresDescricao(),
                quadroClinico.getMedicamentos()
        );
    }

    public QuadroClinico toEntity() {
        QuadroClinico quadroClinico = new QuadroClinico();
        quadroClinico.setQueixaPrincipal(queixaPrincipal);
        quadroClinico.setHistoriaDoencaAtual(historiaDoencaAtual);
        quadroClinico.setHistoricoSaude(historicoSaude);
        quadroClinico.setCirurgias(cirurgias);
        quadroClinico.setCirurgiasDescricao(cirurgiasDescricao);
        quadroClinico.setLesoesAnteriores(lesoesAnteriores);
        quadroClinico.setLesoesAnterioresDescricao(lesoesAnterioresDescricao);
        quadroClinico.setMedicamentos(medicamentos);
        return quadroClinico;
    }
}
