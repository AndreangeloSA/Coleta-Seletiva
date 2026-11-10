package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ItemConclusaoDTO {

    @NotNull(message = "O ID do item é obrigatório")
    @Positive(message = "O ID do item deve ser maior que zero")
    private Long itemId;

    @NotNull(message = "A quantidade efetiva é obrigatória")
    @PositiveOrZero(message = "A quantidade efetiva não pode ser negativa")
    private BigDecimal quantidadeEfetiva;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public BigDecimal getQuantidadeEfetiva() {
        return quantidadeEfetiva;
    }

    public void setQuantidadeEfetiva(BigDecimal quantidadeEfetiva) {
        this.quantidadeEfetiva = quantidadeEfetiva;
    }
}