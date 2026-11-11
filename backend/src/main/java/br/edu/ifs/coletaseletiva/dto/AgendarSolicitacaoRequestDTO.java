package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.OffsetDateTime;

public class AgendarSolicitacaoRequestDTO {
    @NotNull(message = "A data e hora do agendamento são obrigatórias.")
    private OffsetDateTime dataAgendamento;

    @NotNull(message = "A versão atual da solicitação é obrigatória.")
    @PositiveOrZero(message = "A versão não pode ser negativa.")
    private Long versao;

    public OffsetDateTime getDataAgendamento() { return dataAgendamento; }
    public void setDataAgendamento(OffsetDateTime dataAgendamento) { this.dataAgendamento = dataAgendamento; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
