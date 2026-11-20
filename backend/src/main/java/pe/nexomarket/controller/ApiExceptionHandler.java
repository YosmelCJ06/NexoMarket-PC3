package pe.nexomarket.controller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.nexomarket.domain.pedido.PedidoNoEncontradoException;
import pe.nexomarket.domain.pedido.TransicionInvalidaException;
import pe.nexomarket.domain.seguridad.AccesoDenegadoException;

/**
 * Convierte las excepciones del dominio en respuestas HTTP con un mensaje claro,
 * para que los controladores no repitan bloques try/catch.
 */
@RestControllerAdvice(assignableTypes = {PedidoPatronesController.class, ReporteController.class,
        CheckoutController.class})
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<Map<String, String>> accesoDenegado(AccesoDenegadoException error) {
        return respuesta(HttpStatus.FORBIDDEN, error.getMessage());
    }

    @ExceptionHandler(PedidoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(PedidoNoEncontradoException error) {
        return respuesta(HttpStatus.NOT_FOUND, error.getMessage());
    }

    /** State rechazó la transición, o no hay nada que deshacer. */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflicto(IllegalStateException error) {
        return respuesta(HttpStatus.CONFLICT, error.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> peticionInvalida(IllegalArgumentException error) {
        return respuesta(HttpStatus.BAD_REQUEST, error.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> baseDeDatos(DataAccessException error) {
        log.error("Error de base de datos: {}", error.getMostSpecificCause().getMessage());
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo completar la operación en PostgreSQL.");
    }

    private static ResponseEntity<Map<String, String>> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(Map.of("message", mensaje));
    }
}
