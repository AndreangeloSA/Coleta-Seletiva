package br.edu.ifs.coletaseletiva.dto;
import jakarta.validation.constraints.*;
public record PontoRequestDTO(@NotBlank @Size(max=255) String nome,@NotBlank @Size(max=500) String endereco,@Size(max=255) String municipio,@Size(max=255) String bairro,@Size(max=255) String contato,@Size(max=500) String horariosFuncionamento,Boolean ativo,boolean recolhimentoDomiciliar){}
