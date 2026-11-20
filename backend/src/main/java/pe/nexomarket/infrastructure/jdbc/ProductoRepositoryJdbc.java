package pe.nexomarket.infrastructure.jdbc;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.nexomarket.domain.catalogo.ProductoHoja;
import pe.nexomarket.domain.catalogo.ProductoRepository;

/** Acceso a la tabla producto para el inventario del checkout. */
@Repository
public class ProductoRepositoryJdbc implements ProductoRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProductoRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ProductoHoja> buscarPorId(int productoId) {
        return jdbcTemplate.query("SELECT id_producto, nombre, precio, stock FROM producto WHERE id_producto = ?",
                (fila, numero) -> new ProductoHoja(fila.getInt("id_producto"), fila.getString("nombre"),
                        fila.getBigDecimal("precio"), fila.getInt("stock")), productoId).stream().findFirst();
    }

    @Override
    public boolean descontarStock(int productoId, int cantidad) {
        return jdbcTemplate.update("UPDATE producto SET stock = stock - ? WHERE id_producto = ? AND stock >= ?",
                cantidad, productoId, cantidad) == 1;
    }
}
