package br.edu.imepac.agendamento.controllers;

import br.edu.imepac.agendamento.dtos.ErrorResponse;
import br.edu.imepac.commons.exceptions.BusinessException;
import br.edu.imepac.commons.exceptions.EntityNotFoundException;
import br.edu.imepac.commons.exceptions.IntegrationException;
import br.edu.imepac.commons.exceptions.ServicoIndisponivelException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("Validacao falhou: {} campo(s) invalido(s)", ex.getBindingResult().getFieldErrorCount());
        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                campos.put(error.getField(), error.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, campos);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        log.warn("Recurso nao encontrado: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        log.warn("Regra de negocio violada: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<ErrorResponse> handleIntegration(IntegrationException ex) {
        log.error("Falha de integracao: {}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn("Conflito de dados: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento invalido: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex) {
        log.warn("Recurso nao encontrado: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponse> handleDateParse(DateTimeParseException ex) {
        log.warn("Formato de data invalido: {}", ex.getParsedString());
        return build(HttpStatus.BAD_REQUEST, "Formato de data invalido: " + ex.getParsedString());
    }

    // violacao de constraint do banco (unique, FK, not null) — vira 409 ao inves de 500
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violacao de integridade no banco: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Violacao de integridade: registro duplicado ou referencia invalida");
    }

    // JSON malformado no body — 400 ao inves de 500
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("JSON malformado: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.BAD_REQUEST, "JSON malformado ou tipo de campo invalido");
    }

    // path/query param com tipo errado (ex.: id=abc) — 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Parametro {} com tipo invalido: {}", ex.getName(), ex.getValue());
        return build(HttpStatus.BAD_REQUEST,
                "Parametro '" + ex.getName() + "' com tipo invalido");
    }

    // query param obrigatorio ausente — 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Parametro obrigatorio ausente: {}", ex.getParameterName());
        return build(HttpStatus.BAD_REQUEST,
                "Parametro obrigatorio ausente: " + ex.getParameterName());
    }

    // validacao em path/query params (@Validated no controller) — 400
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Validacao de parametro falhou: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // metodo HTTP nao suportado pela rota — 405
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        log.warn("Metodo HTTP nao suportado: {}", ex.getMethod());
        return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    // microsservico externo (administrativo) indisponivel — 503
    @ExceptionHandler(ServicoIndisponivelException.class)
    public ResponseEntity<ErrorResponse> handleServicoIndisponivel(ServicoIndisponivelException ex) {
        log.error("Servico externo indisponivel: {}", ex.getMessage());
        return build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    // fallback — qualquer excecao nao mapeada vira 500 generico, sem vazar stack pro cliente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Erro inesperado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, Object message) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );
        return ResponseEntity.status(status).body(body);
    }
}
