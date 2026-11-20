package pe.nexomarket.domain.pedido;

import java.math.BigDecimal;

/** Una línea del pedido: producto, cantidad y precio unitario al momento de la compra. */
public record LineaPedido(int productoId, int cantidad, BigDecimal precioUnitario) {

    public LineaPedido {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        if (precioUnitario == null || precioUnitario.signum() < 0) {
            throw new IllegalArgumentException("El precio unitario no puede ser negativo.");
        }
    }

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
