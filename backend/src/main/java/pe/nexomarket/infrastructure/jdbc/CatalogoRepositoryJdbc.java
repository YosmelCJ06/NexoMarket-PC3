package pe.nexomarket.infrastructure.jdbc;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pe.nexomarket.domain.catalogo.CatalogoRepository;
import pe.nexomarket.domain.catalogo.CategoriaComposite;
import pe.nexomarket.domain.catalogo.ProductoHoja;

/** Arma el árbol Composite del catálogo a partir de las tablas producto y categoria. */
@Repository
public class CatalogoRepositoryJdbc implements CatalogoRepository {
    private final JdbcTemplate jdbcTemplate;

    public CatalogoRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CategoriaComposite cargarCatalogo() {
        CategoriaComposite raiz = new CategoriaComposite("Catálogo NexoMarket");
        Map<String, CategoriaComposite> categorias = new LinkedHashMap<>();
        jdbcTemplate.query("""
                SELECT p.id_producto, p.nombre, c.nombre_categoria, p.precio, p.stock
                FROM producto p
                JOIN categoria c ON c.id_categoria = p.id_categoria
                ORDER BY c.nombre_categoria, p.id_producto
                """, fila -> {
            CategoriaComposite categoria = categorias.computeIfAbsent(fila.getString("nombre_categoria"), nombre -> {
                CategoriaComposite nueva = new CategoriaComposite(nombre);
                raiz.agregar(nueva);
                return nueva;
            });
            categoria.agregar(new ProductoHoja(fila.getInt("id_producto"), fila.getString("nombre"),
                    fila.getBigDecimal("precio"), fila.getInt("stock")));
        });
        return raiz;
    }
}
