package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.constraints.*;
import java.util.Locale;

public class EnderecoRequestDTO {
    @NotBlank @Size(max = 255)
    private String logradouro;

    @Size(max = 255)
    private String numero;

    private boolean semNumero;

    @NotBlank @Size(max = 255)
    private String bairro;

    @NotBlank @Size(max = 255)
    private String municipio;

    @NotBlank @Pattern(regexp = "[A-Z]{2}")
    private String uf;

    @NotBlank @Pattern(regexp = "[0-9]{5}-?[0-9]{3}")
    private String cep;

    @Size(max = 255)
    private String complemento;

    public EnderecoRequestDTO() {}

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero == null || numero.isBlank() ? null : numero.strip(); }

    public boolean isSemNumero() { return semNumero; }
    public void setSemNumero(Boolean semNumero) { this.semNumero = Boolean.TRUE.equals(semNumero); }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getMunicipio() { return municipio; }
    public void setMunicipio(String municipio) { this.municipio = municipio; }

    public String getUf() { return uf; }
    public void setUf(String uf) { this.uf = uf == null ? null : uf.strip().toUpperCase(Locale.ROOT); }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    @AssertTrue(message = "Informe o número ou marque semNumero")
    public boolean isNumeracaoValida() {
        return semNumero == (numero == null);
    }
}