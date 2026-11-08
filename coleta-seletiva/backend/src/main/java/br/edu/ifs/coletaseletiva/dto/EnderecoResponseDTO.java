package br.edu.ifs.coletaseletiva.dto;

import br.edu.ifs.coletaseletiva.entity.Endereco;

public class EnderecoResponseDTO {
    private Long id;

    private Long moradorId;

    private String logradouro;

    private String numero;

    private boolean semNumero;

    private String bairro;

    private String municipio;

    private String uf;

    private String cep;

    private String complemento;

    public EnderecoResponseDTO() {}

    public EnderecoResponseDTO(Endereco endereco) {
        this.id = endereco.getId();
        this.moradorId = endereco.getMoradorId();
        this.logradouro = endereco.getLogradouro();
        this.numero = endereco.getNumero();
        this.semNumero = endereco.isSemNumero();
        this.bairro = endereco.getBairro();
        this.municipio = endereco.getMunicipio();
        this.uf = endereco.getUf();
        this.cep = endereco.getCep();
        this.complemento = endereco.getComplemento();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMoradorId() { return moradorId; }
    public void setMoradorId(Long moradorId) { this.moradorId = moradorId; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public boolean isSemNumero() { return semNumero; }
    public void setSemNumero(boolean semNumero) { this.semNumero = semNumero; }

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
}