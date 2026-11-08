package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.constraints.NotBlank;

public class CancelarSolicitacaoRequestDTO {

    @NotBlank(message = "O motivo do cancelamento é obrigatório.")
    private String motivo;

    public CancelarSolicitacaoRequestDTO() {}

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
