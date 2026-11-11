package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public class ReagendarSolicitacaoRequestDTO {
    @NotBlank(message = "A justificativa do reagendamento é obrigatória.")
    @Size(max = 255, message = "A justificativa deve ter no máximo 255 caracteres.")
    private String motivo;

    @NotNull(message = "A nova data e hora são obrigatórias.")
    private OffsetDateTime novaDataHora;

    @NotNull(message = "A versão atual da solicitação é obrigatória.")
    @PositiveOrZero(message = "A versão não pode ser negativa.")
    private Long versao;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public OffsetDateTime getNovaDataHora() { return novaDataHora; }
    public void setNovaDataHora(OffsetDateTime novaDataHora) { this.novaDataHora = novaDataHora; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
