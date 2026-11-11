package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class VersaoRequestDTO {
    @NotNull(message = "A versão atual da solicitação é obrigatória.")
    @PositiveOrZero(message = "A versão não pode ser negativa.")
    private Long versao;

    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
