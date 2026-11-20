package pe.nexomarket.controller;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final List<String> ORDER_STATUSES = List.of(
            "PENDIENTE", "PAGADO", "CONFIRMADO", "PREPARANDO", "ENVIADO", "ENTREGADO", "CANCELADO");

    private final JdbcTemplate jdbcTemplate;

    public OrderController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<?> listOrders(
            @RequestParam(name = "estado", required = false) String requestedStatus,
            HttpSession session) {
        Integer userId = (Integer) session.getAttribute("nexomarket.userId");
        String role = (String) session.getAttribute("nexomarket.role");
        if (userId == null || role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!role.equals("CLIENTE") && !role.equals("ADMINISTRADOR")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String status = requestedStatus == null || requestedStatus.isBlank()
            || requestedStatus.equalsIgnoreCase("TODOS")
            ? null
            : requestedStatus.trim().toUpperCase(Locale.ROOT);
        if (status != null && !ORDER_STATUSES.contains(status)) {
            return ResponseEntity.badRequest().build();
        }

        StringBuilder query = new StringBuilder("""
            SELECT p.id_pedido, c.nombre AS cliente_nombre, c.email AS cliente_email,
                   p.monto_total, p.estado, p.fecha_pedido
            FROM pedido p
            JOIN cliente c ON c.id_cliente = p.id_cliente
            WHERE 1 = 1
            """);
        List<Object> parameters = new ArrayList<>();

        if (role.equals("CLIENTE")) {
            query.append("""
                     AND EXISTS (
                         SELECT 1
                         FROM usuario u
                         WHERE u.id_usuario = ?
                           AND (u.id_cliente = p.id_cliente OR LOWER(u.email) = LOWER(c.email))
                     )
                    """);
            parameters.add(userId);
        }
        if (status != null) {
            query.append(" AND p.estado = ?");
            parameters.add(status);
        }
        query.append(" ORDER BY p.fecha_pedido DESC, p.id_pedido DESC");

        List<OrderSummary> orders = jdbcTemplate.query(query.toString(), (resultSet, rowNumber) ->
            new OrderSummary(
                resultSet.getInt("id_pedido"),
                resultSet.getString("cliente_nombre"),
                resultSet.getString("cliente_email"),
                resultSet.getBigDecimal("monto_total"),
                resultSet.getString("estado"),
                resultSet.getTimestamp("fecha_pedido").toInstant()),
            parameters.toArray());

        List<OrderResponse> response = orders.stream()
            .map(order -> new OrderResponse(
                order.id(),
                order.customerName(),
                order.customerEmail(),
                order.total(),
                order.status(),
                order.createdAt(),
                jdbcTemplate.query("""
                    SELECT p.nombre, dp.cantidad, dp.precio_unitario
                    FROM detalle_pedido dp
                    JOIN producto p ON p.id_producto = dp.id_producto
                    WHERE dp.id_pedido = ?
                    ORDER BY dp.id_detalle_pedido
                    """, (resultSet, rowNumber) -> new OrderItemResponse(
                    resultSet.getString("nombre"),
                    resultSet.getInt("cantidad"),
                    resultSet.getBigDecimal("precio_unitario")), order.id())))
            .toList();

        return ResponseEntity.ok(response);
        }

        private record OrderSummary(
            int id,
            String customerName,
            String customerEmail,
            BigDecimal total,
            String status,
            Instant createdAt) {
        }

        public record OrderItemResponse(String name, int quantity, BigDecimal price) {
        }

        public record OrderResponse(
            int id,
            String customerName,
            String customerEmail,
            BigDecimal total,
            String status,
            Instant createdAt,
            List<OrderItemResponse> items) {
        }
    }
