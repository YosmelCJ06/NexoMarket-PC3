package pe.nexomarket.controller;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final JdbcTemplate jdbcTemplate;
        private final String productQuery = """
            SELECT p.id_producto, p.nombre, c.nombre_categoria, p.precio, p.stock
            FROM producto p
            JOIN categoria c ON c.id_categoria = p.id_categoria
            ORDER BY p.id_producto
            """;

    public ProductController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<ProductResponse> listProducts() {
        return jdbcTemplate.query(productQuery, this::mapProduct);
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductRequest request, HttpSession session) {
        if (!isAdministrator(session)) return forbidden();
        if (request == null || request.name() == null || request.name().isBlank()
                || request.category() == null || request.category().isBlank()
                || request.price() == null || request.price().signum() <= 0
                || request.stock() == null || request.stock() < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Datos de producto inválidos."));
        }

        jdbcTemplate.update("""
                INSERT INTO categoria (nombre_categoria)
                VALUES (?)
                ON CONFLICT (nombre_categoria) DO NOTHING
                """, request.category().trim());

        Integer productId = jdbcTemplate.queryForObject("""
                INSERT INTO producto (id_categoria, nombre, precio, stock)
                SELECT id_categoria, ?, ?, ?
                FROM categoria
                WHERE nombre_categoria = ?
                RETURNING id_producto
                """, Integer.class, request.name().trim(), request.price(), request.stock(), request.category().trim());

        return ResponseEntity.status(HttpStatus.CREATED).body(findProduct(productId));
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable int id, @RequestBody StockRequest request, HttpSession session) {
        if (!isAdministrator(session)) return forbidden();
        if (request == null || request.stock() == null || request.stock() < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "El stock debe ser cero o mayor."));
        }

        int updatedRows = jdbcTemplate.update("UPDATE producto SET stock = ? WHERE id_producto = ?", request.stock(), id);
        if (updatedRows == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(findProduct(id));
    }

    private ProductResponse findProduct(int id) {
        return jdbcTemplate.queryForObject("""
                SELECT p.id_producto, p.nombre, c.nombre_categoria, p.precio, p.stock
                FROM producto p
                JOIN categoria c ON c.id_categoria = p.id_categoria
                WHERE p.id_producto = ?
                """, this::mapProduct, id);
    }

    private ProductResponse mapProduct(java.sql.ResultSet resultSet, int rowNumber) throws java.sql.SQLException {
        return new ProductResponse(
                resultSet.getInt("id_producto"),
                resultSet.getString("nombre"),
                resultSet.getString("nombre_categoria"),
                resultSet.getBigDecimal("precio"),
                resultSet.getInt("stock"));
    }

    private boolean isAdministrator(HttpSession session) {
        return "ADMINISTRADOR".equals(session.getAttribute("nexomarket.role"));
    }

    private ResponseEntity<Map<String, String>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Se requiere una sesión de administrador."));
    }

    public record ProductResponse(int id, String name, String category, BigDecimal price, int stock) {
    }

    public record ProductRequest(String name, String category, BigDecimal price, Integer stock) {
    }

    public record StockRequest(Integer stock) {
    }
}
