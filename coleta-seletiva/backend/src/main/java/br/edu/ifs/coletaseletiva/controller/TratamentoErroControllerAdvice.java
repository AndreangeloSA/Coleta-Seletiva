package br.edu.ifs.coletaseletiva.controller;

import br.edu.ifs.coletaseletiva.dto.ErroApiDTO;
import br.edu.ifs.coletaseletiva.dto.ErroCampoDTO;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@RestControllerAdvice
public class TratamentoErroControllerAdvice {
    private static final ZoneId FUSO = ZoneId.of("America/Maceio");

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroApiDTO> tratarValidacao(MethodArgumentNotValidException ex) {
        List<ErroCampoDTO> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroCampoDTO(erro.getField(), erro.getDefaultMessage()))
                .distinct().toList();
        return resposta(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", "Existem campos inválidos na requisição.", erros);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroApiDTO> tratarConstraint(ConstraintViolationException ex) {
        List<ErroCampoDTO> erros = ex.getConstraintViolations().stream()
                .map(erro -> new ErroCampoDTO(erro.getPropertyPath().toString(), erro.getMessage())).toList();
        return resposta(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", "Existem parâmetros inválidos.", erros);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroApiDTO> tratarResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String mensagem = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
        return resposta(status, codigo(status), mensagem, List.of());
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ErroApiDTO> tratarConcorrencia(Exception ex) {
        return resposta(HttpStatus.CONFLICT, "VERSAO_DESATUALIZADA",
                "A solicitação foi alterada por outra operação. Consulte novamente antes de atualizar.", List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroApiDTO> tratarIntegridade(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT, "CONFLITO_INTEGRIDADE",
                "A operação viola uma restrição de integridade do banco de dados.", List.of());
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErroApiDTO> tratarParametroInvalido(Exception ex) {
        return resposta(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO",
                "Um parâmetro obrigatório está ausente ou possui formato inválido.", List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroApiDTO> tratarJsonInvalido(HttpMessageNotReadableException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "JSON_INVALIDO",
                "O corpo da requisição está ausente ou possui formato inválido.", List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroApiDTO> tratarArgumento(IllegalArgumentException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "ARGUMENTO_INVALIDO", mensagemSegura(ex), List.of());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroApiDTO> tratarEstado(IllegalStateException ex) {
        return resposta(HttpStatus.CONFLICT, "CONFLITO_REGRA_NEGOCIO", mensagemSegura(ex), List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroApiDTO> tratarErroInterno(Exception ex) {
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Ocorreu um erro interno. Tente novamente ou contate o responsável pelo sistema.", List.of());
    }

    private ResponseEntity<ErroApiDTO> resposta(HttpStatus status, String codigo, String mensagem, List<ErroCampoDTO> erros) {
        return ResponseEntity.status(status).body(new ErroApiDTO(
                OffsetDateTime.now(FUSO), status.value(), codigo, mensagem, erros));
    }

    private String codigo(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "REQUISICAO_INVALIDA";
            case NOT_FOUND -> "REGISTRO_NAO_ENCONTRADO";
            case CONFLICT -> "CONFLITO_REGRA_NEGOCIO";
            default -> "HTTP_" + status.value();
        };
    }

    private String mensagemSegura(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank() ? "A requisição não pode ser processada." : ex.getMessage();
    }
}
