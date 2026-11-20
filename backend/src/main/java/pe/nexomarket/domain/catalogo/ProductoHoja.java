package pe.nexomarket.domain.catalogo;

import java.math.BigDecimal;
import java.util.List;

/** Patrón COMPOSITE — participante "Leaf": un producto del catálogo. */
public class ProductoHoja implements NodoCatalogo {
    private final int id;
    private final String nombre;
    private final BigDecimal precio;
    private final int stock;

    public ProductoHoja(int id, String nombre, BigDecimal precio, int stock) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío.");
        }
        if (precio == null || precio.signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        if (stock < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
        this.id = id;
        this.nombre = nombre.trim();
        this.precio = precio;
        this.stock = stock;
    }

    @Override
    public String obtenerNombre() {
        return nombre;
    }

    @Override
    public BigDecimal obtenerPrecioTotal() {
        return precio;
    }

    @Override
    public int contarProductos() {
        return 1;
    }

    @Override
    public int obtenerStockTotal() {
        return stock;
    }

    @Override
    public List<ProductoHoja> obtenerProductos() {
        return List.of(this);
    }

    public int getId() {
        return id;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public int getStock() {
        return stock;
    }
}
