package com.puckzone.ranking.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * Manejo centralizado de errores para todos los controladores:
 * recurso inexistente → 404, payload inválido → 400 con el detalle por campo,
 * argumentos ilegales (reglas de negocio violadas) → 400.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        StringBuilder detail = new StringBuilder("Payload inválido:");
        ex.getBindingResult().getFieldErrors().forEach(error ->
                detail.append(" [").append(error.getField()).append(": ")
                        .append(error.getDefaultMessage()).append("]"));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail.toString());
    }
}
