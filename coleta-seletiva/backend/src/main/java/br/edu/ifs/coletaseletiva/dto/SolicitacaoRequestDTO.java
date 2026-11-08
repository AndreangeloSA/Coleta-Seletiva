package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class SolicitacaoRequestDTO {

    @NotNull(message = "O ID do ponto de coleta é obrigatório.")
    private Long pontoId;

    private String observacoes;

    @NotEmpty(message = "A solicitação deve conter pelo menos um item.")
    @Valid
    private List<ItemSolicitacaoRequestDTO> itens;

    public SolicitacaoRequestDTO() {}

    public Long getPontoId() { return pontoId; }
    public void setPontoId(Long pontoId) { this.pontoId = pontoId; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public List<ItemSolicitacaoRequestDTO> getItens() { return itens; }
    public void setItens(List<ItemSolicitacaoRequestDTO> itens) { this.itens = itens; }
}