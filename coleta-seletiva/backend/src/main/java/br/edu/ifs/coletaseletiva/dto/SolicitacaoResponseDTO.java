package br.edu.ifs.coletaseletiva.dto;

import br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao;
import java.time.OffsetDateTime;
import java.util.List;

public class SolicitacaoResponseDTO {
    private Long id;
    private String protocolo;
    private Long moradorId;
    private Long pontoId;
    private String logradouro;
    private String numero;
    private boolean semNumero;
    private String bairro;
    private String municipio;
    private String uf;
    private String cep;
    private String complemento;
    private SituacaoSolicitacao situacao;
    private OffsetDateTime dataCriacao;
    private OffsetDateTime dataAgendamento;
    private OffsetDateTime dataAtualizacao;
    private Long versao;
    private String observacoes;
    private List<ItemSolicitacaoResponseDTO> itens;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProtocolo() { return protocolo; }
    public void setProtocolo(String protocolo) { this.protocolo = protocolo; }
    public Long getMoradorId() { return moradorId; }
    public void setMoradorId(Long moradorId) { this.moradorId = moradorId; }
    public Long getPontoId() { return pontoId; }
    public void setPontoId(Long pontoId) { this.pontoId = pontoId; }
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public boolean isSemNumero() { return semNumero; }
    public void setSemNumero(boolean semNumero) { this.semNumero = semNumero; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }
    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }
    public SituacaoSolicitacao getSituacao() { return situacao; }
    public void setSituacao(SituacaoSolicitacao situacao) { this.situacao = situacao; }
    public OffsetDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(OffsetDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
    public OffsetDateTime getDataAgendamento() { return dataAgendamento; }
    public void setDataAgendamento(OffsetDateTime dataAgendamento) { this.dataAgendamento = dataAgendamento; }
    public OffsetDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(OffsetDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public List<ItemSolicitacaoResponseDTO> getItens() { return itens; }
    public void setItens(List<ItemSolicitacaoResponseDTO> itens) { this.itens = itens; }
}
