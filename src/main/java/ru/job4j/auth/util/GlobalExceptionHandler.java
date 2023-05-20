package ru.job4j.auth.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Аннотация @ControllerAdvice,
 * используемая совместно с @ExceptionHandler,
 * позволяет обрабатывать все исключения
 * (в данном случае - NullPointerException),
 * которые возникают во всех контроллерах.
 */
@ControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler {
    private static final Logger LOG = LogManager.getLogger(GlobalExceptionHandler.class.getName());
    private final ObjectMapper objectMapper;

    @ExceptionHandler(value = {NullPointerException.class})
    public void nullPointerExceptionHandler(Exception e, HttpServletRequest request,
                                            HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new HashMap<>() {
            {
                put("message", "Some of fields empty");
                put("details", e.getMessage());
            }
        }));
        LOG.error(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(
                e.getFieldErrors().stream()
                        .map(f -> Map.of(
                                f.getField(),
                                String.format("%s. Actual value: %s", f.getDefaultMessage(), f.getRejectedValue())
                        ))
                        .collect(Collectors.toList())
        );
    }
}