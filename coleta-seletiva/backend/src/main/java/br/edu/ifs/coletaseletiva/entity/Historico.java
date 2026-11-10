package br.edu.ifs.coletaseletiva.entity;

import br.edu.ifs.coletaseletiva.enums.SituacaoSolicitacao;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "historico")
public class Historico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private Solicitacao solicitacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_anterior", length = 50)
    private SituacaoSolicitacao situacaoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao_nova", nullable = false, length = 50)
    private SituacaoSolicitacao situacaoNova;

    @Column(name = "data_hora", nullable = false)
    private OffsetDateTime dataHora;

    private String responsavel;
    private String motivo;

    @Column(name = "dados_tentativa")
    private String dadosTentativa;

    public Historico() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Solicitacao getSolicitacao() { return solicitacao; }
    public void setSolicitacao(Solicitacao solicitacao) { this.solicitacao = solicitacao; }

    public SituacaoSolicitacao getSituacaoAnterior() { return situacaoAnterior; }
    public void setSituacaoAnterior(SituacaoSolicitacao situacaoAnterior) { this.situacaoAnterior = situacaoAnterior; }

    public SituacaoSolicitacao getSituacaoNova() { return situacaoNova; }
    public void setSituacaoNova(SituacaoSolicitacao situacaoNova) { this.situacaoNova = situacaoNova; }

    public OffsetDateTime getDataHora() { return dataHora; }
    public void setDataHora(OffsetDateTime dataHora) { this.dataHora = dataHora; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDadosTentativa() { return dadosTentativa; }
    public void setDadosTentativa(String dadosTentativa) { this.dadosTentativa = dadosTentativa; }
}
