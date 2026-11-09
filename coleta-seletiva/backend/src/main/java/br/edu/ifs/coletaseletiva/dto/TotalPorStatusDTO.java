package br.edu.ifs.coletaseletiva.dto;

import br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao;

public class TotalPorStatusDTO {

    private SituacaoSolicitacao situacao;
    private Long quantidade;

    public TotalPorStatusDTO() {
    }

    public TotalPorStatusDTO(SituacaoSolicitacao situacao, Long quantidade) {
        this.situacao = situacao;
        this.quantidade = quantidade;
    }

    public SituacaoSolicitacao getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoSolicitacao situacao) {
        this.situacao = situacao;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }
}