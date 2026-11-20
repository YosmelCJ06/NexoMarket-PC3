package pe.nexomarket.domain.pedido;

/** El pedido solicitado no existe. */
public class PedidoNoEncontradoException extends RuntimeException {
    public PedidoNoEncontradoException(int pedidoId) {
        super("No existe el pedido " + pedidoId + ".");
    }
}
