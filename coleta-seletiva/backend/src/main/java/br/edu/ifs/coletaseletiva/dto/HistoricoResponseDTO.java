package br.edu.ifs.coletaseletiva.dto;

import br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao;
import java.time.LocalDateTime;

public class HistoricoResponseDTO {

    private Long id;
    private Long solicitacaoId;
    private SituacaoSolicitacao situacaoAnterior;
    private SituacaoSolicitacao situacaoNova;
    private LocalDateTime dataHora;
    private String responsavel;
    private String motivo;
    private String dadosTentativa;

    public HistoricoResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSolicitacaoId() { return solicitacaoId; }
    public void setSolicitacaoId(Long solicitacaoId) { this.solicitacaoId = solicitacaoId; }

    public SituacaoSolicitacao getSituacaoAnterior() { return situacaoAnterior; }
    public void setSituacaoAnterior(SituacaoSolicitacao situacaoAnterior) { this.situacaoAnterior = situacaoAnterior; }

    public SituacaoSolicitacao getSituacaoNova() { return situacaoNova; }
    public void setSituacaoNova(SituacaoSolicitacao situacaoNova) { this.situacaoNova = situacaoNova; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDadosTentativa() { return dadosTentativa; }
    public void setDadosTentativa(String dadosTentativa) { this.dadosTentativa = dadosTentativa; }
}