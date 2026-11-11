package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class CancelarSolicitacaoRequestDTO {

    @NotBlank(message = "O motivo do cancelamento é obrigatório.")
    @Size(max = 255, message = "O motivo deve ter no máximo 255 caracteres.")
    private String motivo;

    @NotNull(message = "A versão atual da solicitação é obrigatória.")
    @PositiveOrZero(message = "A versão não pode ser negativa.")
    private Long versao;

    public CancelarSolicitacaoRequestDTO() {}

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
