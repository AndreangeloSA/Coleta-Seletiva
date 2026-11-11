package br.edu.ifs.coletaseletiva.dto;
import br.edu.ifs.coletaseletiva.entity.MaterialAceito;
import java.math.BigDecimal;
public record MaterialAceitoResponseDTO(Long id,Long pontoId,Long materialId,String nomeMaterial,String unidadeMedida,String observacoes,BigDecimal limitePorSolicitacao){public MaterialAceitoResponseDTO(MaterialAceito m){this(m.getId(),m.getPontoId(),m.getMaterialId(),m.getNomeMaterial(),m.getUnidadeMedida(),m.getObservacoes(),m.getLimitePorSolicitacao());}}
