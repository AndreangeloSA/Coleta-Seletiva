package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ConcluirSolicitacaoRequestDTO {

    @NotEmpty(message = "A lista de itens recolhidos não pode estar vazia")
    @Valid
    private List<ItemConclusaoDTO> itens;

    private String observacoes;

    public List<ItemConclusaoDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemConclusaoDTO> itens) {
        this.itens = itens;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}