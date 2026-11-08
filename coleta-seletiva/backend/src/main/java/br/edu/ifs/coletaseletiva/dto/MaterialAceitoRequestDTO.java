package br.edu.ifs.coletaseletiva.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record MaterialAceitoRequestDTO(@Size(max=1000) String observacoes,@DecimalMin(value="0.001") BigDecimal limitePorSolicitacao){}
