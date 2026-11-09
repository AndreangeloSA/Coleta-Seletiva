package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.constraints.NotBlank;

public class RegistrarFrustracaoRequestDTO {

    @NotBlank(message = "O motivo da tentativa frustrada é obrigatório")
    private String motivo;

    private String dadosTentativa;

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDadosTentativa() {
        return dadosTentativa;
    }

    public void setDadosTentativa(String dadosTentativa) {
        this.dadosTentativa = dadosTentativa;
    }
}