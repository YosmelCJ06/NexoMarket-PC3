package pe.nexomarket.controller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> databaseHealth() {
        try {
            String database = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
            return ResponseEntity.ok(Map.of("status", "ok", "database", database));
        } catch (DataAccessException exception) {
            logger.error("PostgreSQL health check failed: {}", exception.getMostSpecificCause().getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "unavailable", "message", "No se pudo conectar a PostgreSQL."));
        }
    }
}