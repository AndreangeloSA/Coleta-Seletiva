package br.edu.ifs.coletaseletiva.dto;
import br.edu.ifs.coletaseletiva.entity.Material;
public record MaterialResponseDTO(Long id,String nome,String descricao,String unidadeMedida,String orientacaoPreparo,boolean ativo){public MaterialResponseDTO(Material m){this(m.getId(),m.getNome(),m.getDescricao(),m.getUnidadeMedida(),m.getOrientacaoPreparo(),m.isAtivo());}}
