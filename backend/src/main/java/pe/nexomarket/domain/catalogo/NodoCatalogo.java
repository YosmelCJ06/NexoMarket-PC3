package pe.nexomarket.domain.catalogo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Patrón COMPOSITE — participante "Component".
 * Una categoría y un producto se tratan igual: ambos saben dar su nombre,
 * sumar precios, contar productos y listar los productos que contienen.
 */
public interface NodoCatalogo {
    String obtenerNombre();

    BigDecimal obtenerPrecioTotal();

    int contarProductos();

    int obtenerStockTotal();

    List<ProductoHoja> obtenerProductos();
}
