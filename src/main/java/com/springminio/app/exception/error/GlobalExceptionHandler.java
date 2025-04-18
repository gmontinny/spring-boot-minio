package com.springminio.app.exception.error;

import com.springminio.app.exception.ApiError;
import com.springminio.app.exception.FileResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type não é suportado. Tipos de mídia suportados são ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));

        List<String> details = List.of(builder.toString());

        ApiError err = new ApiError(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                LocalDateTime.now(),
                "Tipo de mídia inválido",
                details
        );

        log.error("Erro de tipo de mídia não suportado: {}", builder);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(err);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> details = List.of(ex.getMessage());

        ApiError err = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now(),
                "Requisição JSON malformada",
                details
        );

        log.error("Erro de JSON malformado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError err = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now(),
                "Erros de validação",
                details
        );

        log.error("Erros de validação: {}", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> details = List.of(String.format("Parâmetro %s está faltando", ex.getParameterName()));

        ApiError err = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now(),
                "Parâmetros ausentes",
                details
        );

        log.error("Parâmetro ausente: {}", ex.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    @ExceptionHandler(FileResponseException.class)
    public ResponseEntity<Object> handleFileResponseException(FileResponseException ex) {
        List<String> details = List.of(ex.getMessage());

        ApiError err = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND,
                LocalDateTime.now(),
                "Erro no processamento do arquivo",
                details
        );

        log.error("Erro no processamento do arquivo: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        List<String> details = List.of(ex.getMessage());

        ApiError err = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                LocalDateTime.now(),
                "Erro interno do servidor",
                details
        );

        log.error("Erro interno do servidor: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}