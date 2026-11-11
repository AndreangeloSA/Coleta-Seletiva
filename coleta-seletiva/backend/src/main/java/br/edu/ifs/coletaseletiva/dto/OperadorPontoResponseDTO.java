package br.edu.ifs.coletaseletiva.dto;

import br.edu.ifs.coletaseletiva.entity.OperadorPonto;

public record OperadorPontoResponseDTO(Long id, Long operadorId, Long pontoId, String nomePonto) {
    public OperadorPontoResponseDTO(OperadorPonto vinculo) {
        this(
                vinculo.getId(),
                vinculo.getOperadorId(),
                vinculo.getPontoId(),
                vinculo.getNomePonto()
        );
    }
}
