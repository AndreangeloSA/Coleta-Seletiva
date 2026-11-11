package br.edu.ifs.coletaseletiva.dto;

import br.edu.ifs.coletaseletiva.entity.MaterialAceito;
import java.math.BigDecimal;

/** Dados públicos do material aceito por um ponto, incluindo orientação de preparo. */
public record MaterialAceitoConsultaDTO(
        Long materialId,
        String nomeMaterial,
        String unidadeMedida,
        String orientacaoPreparo,
        String observacoes,
        BigDecimal limitePorSolicitacao
) {
    public MaterialAceitoConsultaDTO(MaterialAceito vinculo) {
        this(
                vinculo.getMaterialId(),
                vinculo.getNomeMaterial(),
                vinculo.getUnidadeMedida(),
                vinculo.getOrientacaoPreparo(),
                vinculo.getObservacoes(),
                vinculo.getLimitePorSolicitacao()
        );
    }
}
