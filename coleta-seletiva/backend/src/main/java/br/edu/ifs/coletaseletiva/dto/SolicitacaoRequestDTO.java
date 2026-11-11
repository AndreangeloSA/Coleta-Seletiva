package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public class SolicitacaoRequestDTO {

    @NotNull(message = "O ID do morador é obrigatório.")
    @Positive(message = "O ID do morador deve ser maior que zero.")
    private Long moradorId;

    @NotNull(message = "O ID do endereço é obrigatório.")
    @Positive(message = "O ID do endereço deve ser maior que zero.")
    private Long enderecoId;

    @NotNull(message = "O ID do ponto de coleta é obrigatório.")
    @Positive(message = "O ID do ponto de coleta deve ser maior que zero.")
    private Long pontoId;

    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.")
    private String observacoes;

    @NotEmpty(message = "A solicitação deve conter pelo menos um item.")
    @Valid
    private List<ItemSolicitacaoRequestDTO> itens;

    public SolicitacaoRequestDTO() {}

    public Long getMoradorId() { return moradorId; }
    public void setMoradorId(Long moradorId) { this.moradorId = moradorId; }

    public Long getEnderecoId() { return enderecoId; }
    public void setEnderecoId(Long enderecoId) { this.enderecoId = enderecoId; }

    public Long getPontoId() { return pontoId; }
    public void setPontoId(Long pontoId) { this.pontoId = pontoId; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public List<ItemSolicitacaoRequestDTO> getItens() { return itens; }
    public void setItens(List<ItemSolicitacaoRequestDTO> itens) { this.itens = itens; }
}
