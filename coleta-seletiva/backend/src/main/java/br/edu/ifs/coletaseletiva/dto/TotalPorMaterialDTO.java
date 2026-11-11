package br.edu.ifs.coletaseletiva.dto;

import java.math.BigDecimal;

public class TotalPorMaterialDTO {

    private Long materialId;
    private String nomeMaterial;
    private BigDecimal totalRecolhido;
    private String unidade;

    public TotalPorMaterialDTO() {
    }

    public TotalPorMaterialDTO(Long materialId, String nomeMaterial, String unidade, BigDecimal totalRecolhido) {
        this.materialId = materialId;
        this.nomeMaterial = nomeMaterial;
        this.unidade = unidade;
        this.totalRecolhido = totalRecolhido;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getNomeMaterial() {
        return nomeMaterial;
    }

    public void setNomeMaterial(String nomeMaterial) {
        this.nomeMaterial = nomeMaterial;
    }

    public String getUnidade() { return unidade; }

    public void setUnidade(String unidade) { this.unidade = unidade; }

    public BigDecimal getTotalRecolhido() {
        return totalRecolhido;
    }

    public void setTotalRecolhido(BigDecimal totalRecolhido) {
        this.totalRecolhido = totalRecolhido;
    }
}
