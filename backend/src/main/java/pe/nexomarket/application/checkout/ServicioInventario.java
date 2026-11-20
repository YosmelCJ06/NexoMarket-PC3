package pe.nexomarket.application.checkout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import pe.nexomarket.domain.catalogo.ProductoHoja;
import pe.nexomarket.domain.catalogo.ProductoRepository;
import pe.nexomarket.domain.catalogo.StockInsuficienteException;
import pe.nexomarket.domain.pedido.LineaPedido;

/** Responsabilidad única: validar el carrito y descontar el stock de cada producto. */
@Service
public class ServicioInventario {
    private final ProductoRepository productos;

    public ServicioInventario(ProductoRepository productos) {
        this.productos = productos;
    }

    /** Descuenta el stock y devuelve las líneas del pedido con el precio actual de cada producto. */
    public List<LineaPedido> reservar(List<ItemCarrito> items) {
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("El carrito está vacío.");
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        for (ItemCarrito item : items) {
            if (item == null || item.cantidad() <= 0) throw new IllegalArgumentException("Cantidad inválida en el carrito.");
            cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
        }
        List<LineaPedido> lineas = new ArrayList<>();
        cantidades.forEach((productoId, cantidad) -> {
            ProductoHoja producto = productos.buscarPorId(productoId)
                    .orElseThrow(() -> new IllegalArgumentException("No existe el producto " + productoId + "."));
            if (!productos.descontarStock(productoId, cantidad)) {
                throw new StockInsuficienteException(producto.obtenerNombre(), cantidad);
            }
            lineas.add(new LineaPedido(productoId, cantidad, producto.getPrecio()));
        });
        return lineas;
    }
}
