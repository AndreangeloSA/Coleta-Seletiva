package br.edu.ifs.coletaseletiva.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "item_solicitacao")
public class ItemSolicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitacao_id")
    private Solicitacao solicitacao;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(nullable = false)
    private String unidade;

    @Column(name = "quantidade_estimada", nullable = false)
    private BigDecimal quantidadeEstimada;

    @Column(name = "quantidade_efetiva")
    private BigDecimal quantidadeEfetiva;

    public ItemSolicitacao() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Solicitacao getSolicitacao() { return solicitacao; }
    public void setSolicitacao(Solicitacao solicitacao) { this.solicitacao = solicitacao; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public BigDecimal getQuantidadeEstimada() { return quantidadeEstimada; }
    public void setQuantidadeEstimada(BigDecimal quantidadeEstimada) { this.quantidadeEstimada = quantidadeEstimada; }

    public BigDecimal getQuantidadeEfetiva() { return quantidadeEfetiva; }
    public void setQuantidadeEfetiva(BigDecimal quantidadeEfetiva) { this.quantidadeEfetiva = quantidadeEfetiva; }
}
