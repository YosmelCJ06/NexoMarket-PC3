package pe.nexomarket.domain.catalogo;

import java.util.Optional;

/** Puerto de acceso a los productos que necesita el inventario del checkout. */
public interface ProductoRepository {
    Optional<ProductoHoja> buscarPorId(int productoId);

    /** Resta stock solo si alcanza; devuelve false si no había unidades suficientes. */
    boolean descontarStock(int productoId, int cantidad);
}
