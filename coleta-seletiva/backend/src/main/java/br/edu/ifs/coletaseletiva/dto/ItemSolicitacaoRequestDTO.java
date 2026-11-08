package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class ItemSolicitacaoRequestDTO {

    @NotNull(message = "O ID do material é obrigatório.")
    private Long materialId;

    @NotBlank(message = "A unidade é obrigatória.")
    private String unidade;

    @NotNull(message = "A quantidade estimada é obrigatória.")
    @Positive(message = "A quantidade estimada deve ser maior que zero.")
    private BigDecimal quantidadeEstimada;

    public ItemSolicitacaoRequestDTO() {}

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public BigDecimal getQuantidadeEstimada() { return quantidadeEstimada; }
    public void setQuantidadeEstimada(BigDecimal quantidadeEstimada) { this.quantidadeEstimada = quantidadeEstimada; }
}