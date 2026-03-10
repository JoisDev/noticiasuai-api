package com.noticiasuai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // TODO: tal vez hacer una clase ErrorDto en vez de Map<String, String>

    @ExceptionHandler(NoticiaNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NoticiaNotFoundException e) {
        return Map.of(
                "error", "No encontrada",
                "mensaje", e.getMessage(),
                "timestamp", LocalDateTime.now().toString()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleGeneral(Exception e) {
        log.error("Error no manejado: ", e);
        return Map.of(
                "error", "Error interno",
                "mensaje", "Ocurrió un error inesperado. Intente de nuevo.",
                "timestamp", LocalDateTime.now().toString()
        );
    }
}
