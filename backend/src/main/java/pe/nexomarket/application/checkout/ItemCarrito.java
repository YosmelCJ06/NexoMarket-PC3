package pe.nexomarket.application.checkout;

/** Producto y cantidad que el cliente envía al confirmar la compra. */
public record ItemCarrito(int productoId, int cantidad) {
}
