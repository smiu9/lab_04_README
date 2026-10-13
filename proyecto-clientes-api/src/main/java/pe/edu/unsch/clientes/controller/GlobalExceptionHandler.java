package pe.edu.unsch.clientes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.util.List;
import java.net.URI;

/**
 * Manejador global de excepciones (RFC 7807 - Problem Detail)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Maneja IllegalArgumentException (DNI duplicado, etc.)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, 
            ex.getMessage()
        );
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Conflict");
        problem.setProperty("timestamp", new java.util.Date());
        return problem;
    }
    
    /**
     * Maneja RuntimeException (Cliente no encontrado, etc.)
     */
    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntime(RuntimeException ex, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, 
            ex.getMessage()
        );
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Not Found");
        problem.setProperty("timestamp", new java.util.Date());
        return problem;
    }
    
    /**
     * Maneja errores de validación (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, 
            "Errores de validación"
        );
        problem.setType(URI.create("about:blank"));
        problem.setTitle("Bad Request");
        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", new java.util.Date());
        return problem;
    }
}
