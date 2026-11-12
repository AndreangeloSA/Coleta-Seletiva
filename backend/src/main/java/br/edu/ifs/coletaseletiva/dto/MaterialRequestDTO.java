package br.edu.ifs.coletaseletiva.dto;
import jakarta.validation.constraints.*;
public record MaterialRequestDTO(@NotBlank @Size(max=255) String nome,@Size(max=2000) String descricao,@NotBlank @Size(max=50) String unidadeMedida,@Size(max=2000) String orientacaoPreparo){}
