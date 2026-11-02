package com.inteliagent.auth.configuration;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.EmptyResultDataAccessException;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    ResponseEntity<ProblemDetail> notFound(Exception e) { return problem(404, "Registro não encontrado"); }
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ProblemDetail> methodNotAllowed(Exception e) { return problem(405, "Operação não disponível"); }
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    ResponseEntity<ProblemDetail> uploadSize(Exception e) { return problem(413, "A imagem excede o tamanho permitido"); }
    @ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException.class)
    ResponseEntity<ProblemDetail> missingPart(Exception e) { return problem(400, "Selecione uma imagem"); }
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    ResponseEntity<ProblemDetail> status(org.springframework.web.server.ResponseStatusException e) { return problem(e.getStatusCode().value(), e.getStatusCode().value()==401 ? "Sessão expirada ou revogada" : "Não foi possível concluir a operação"); }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ProblemDetail> invalid(IllegalArgumentException e) { return problem(400, "Dados inválidos"); }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> forbidden(AccessDeniedException e) { return problem(403, "Acesso negado"); }
    @ExceptionHandler(EmptyResultDataAccessException.class)
    ResponseEntity<ProblemDetail> missing(EmptyResultDataAccessException e) { return problem(404, "Registro não encontrado"); }
    @ExceptionHandler({org.springframework.dao.DataIntegrityViolationException.class, org.springframework.http.converter.HttpMessageNotReadableException.class})
    ResponseEntity<ProblemDetail> invalidPayload(Exception e) { return problem(400, "Dados inválidos"); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> failure(Exception e) {
        org.slf4j.LoggerFactory.getLogger(getClass()).error("Request failed", e);
        return problem(500, "Não foi possível concluir a operação");
    }
    private ResponseEntity<ProblemDetail> problem(int status, String message) { return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(status), message)); }
}
