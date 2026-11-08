package br.edu.ifs.coletaseletiva.dto;

import java.math.BigDecimal;

public class ItemSolicitacaoResponseDTO {

    private Long id;
    private Long materialId;
    private String unidade;
    private BigDecimal quantidadeEstimada;
    private BigDecimal quantidadeEfetiva;

    public ItemSolicitacaoResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public BigDecimal getQuantidadeEstimada() { return quantidadeEstimada; }
    public void setQuantidadeEstimada(BigDecimal quantidadeEstimada) { this.quantidadeEstimada = quantidadeEstimada; }

    public BigDecimal getQuantidadeEfetiva() { return quantidadeEfetiva; }
    public void setQuantidadeEfetiva(BigDecimal quantidadeEfetiva) { this.quantidadeEfetiva = quantidadeEfetiva; }
}