package br.edu.ifs.coletaseletiva.dto;

import jakarta.validation.constraints.*;
import java.util.Locale;

public record DadosEndereco(
        @NotBlank @Size(max = 255) String logradouro,
        @Size(max = 255) String numero,
        Boolean semNumero,
        @NotBlank @Size(max = 255) String bairro,
        @NotBlank @Size(max = 255) String municipio,
        @NotBlank @Pattern(regexp = "[A-Z]{2}") String uf,
        @NotBlank @Pattern(regexp = "[0-9]{5}-?[0-9]{3}") String cep,
        @Size(max = 255) String complemento) {

    public DadosEndereco {
        semNumero = Boolean.TRUE.equals(semNumero);
        numero = numero == null || numero.isBlank() ? null : numero.strip();
        uf = uf == null ? null : uf.strip().toUpperCase(Locale.ROOT);
    }

    @AssertTrue(message = "Informe o número ou marque semNumero")
    public boolean isNumeracaoValida() {
        return semNumero == (numero == null);
    }
}
