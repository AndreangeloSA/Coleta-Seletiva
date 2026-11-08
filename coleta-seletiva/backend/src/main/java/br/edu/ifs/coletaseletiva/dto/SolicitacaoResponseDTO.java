package br.edu.ifs.coletaseletiva.dto;

import br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao;
import java.time.LocalDateTime;
import java.util.List;

public class SolicitacaoResponseDTO {

    private Long id;
    private String protocolo;
    private Long pontoId;
    private SituacaoSolicitacao situacao;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private String observacoes;
    private List<ItemSolicitacaoResponseDTO> itens;

    public SolicitacaoResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProtocolo() { return protocolo; }
    public void setProtocolo(String protocolo) { this.protocolo = protocolo; }

    public Long getPontoId() { return pontoId; }
    public void setPontoId(Long pontoId) { this.pontoId = pontoId; }

    public SituacaoSolicitacao getSituacao() { return situacao; }
    public void setSituacao(SituacaoSolicitacao situacao) { this.situacao = situacao; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public List<ItemSolicitacaoResponseDTO> getItens() { return itens; }
    public void setItens(List<ItemSolicitacaoResponseDTO> itens) { this.itens = itens; }
}
