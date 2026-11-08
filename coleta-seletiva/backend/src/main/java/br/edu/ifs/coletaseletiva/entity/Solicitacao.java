package br.edu.ifs.coletaseletiva.entity;

import br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "solicitacao")
public class Solicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String protocolo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "morador_id")
    private Morador morador;

    // ID ou referência do Ponto de Coleta
    @Column(name = "ponto_id", nullable = false)
    private Long pontoId;

    // Cópia histórica do endereço (RN04)
    @Column(nullable = false)
    private String logradouro;
    private String numero;
    @Column(nullable = false)
    private String bairro;
    @Column(nullable = false)
    private String municipio;
    @Column(nullable = false)
    private String uf;
    @Column(nullable = false)
    private String cep;
    private String complemento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoSolicitacao situacao;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_agendamento")
    private LocalDateTime dataAgendamento;

    private String observacoes;

    // Controle otimista de concorrência (RN06)
    @Version
    private Long versao;

    @OneToMany(mappedBy = "solicitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemSolicitacao> itens = new ArrayList<>();

    @OneToMany(mappedBy = "solicitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Historico> historicos = new ArrayList<>();

    public Solicitacao() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProtocolo() { return protocolo; }
    public void setProtocolo(String protocolo) { this.protocolo = protocolo; }

    public Morador getMorador() { return morador; }
    public void setMorador(Morador morador) { this.morador = morador; }

    public Long getPontoId() { return pontoId; }
    public void setPontoId(Long pontoId) { this.pontoId = pontoId; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

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

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAgendamento() { return dataAgendamento; }
    public void setDataAgendamento(LocalDateTime dataAgendamento) { this.dataAgendamento = dataAgendamento; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }

    public List<ItemSolicitacao> getItens() { return itens; }
    public void setItens(List<ItemSolicitacao> itens) { this.itens = itens; }

    public List<Historico> getHistoricos() { return historicos; }
    public void setHistoricos(List<Historico> historicos) { this.historicos = historicos; }

    public void adicionarItem(ItemSolicitacao item) {
        itens.add(item);
        item.setSolicitacao(this);
    }

    public void adicionarHistorico(Historico h) {
        historicos.add(h);
        h.setSolicitacao(this);
    }

    private LocalDateTime dataAtualizacao;

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}
