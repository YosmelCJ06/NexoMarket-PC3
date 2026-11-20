package pe.nexomarket.application.checkout;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.nexomarket.domain.pedido.LineaPedido;
import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.PedidoRepository;

/** Responsabilidad única: registrar el pedido nuevo con sus líneas. */
@Service
public class ServicioPedido {
    private final PedidoRepository pedidos;

    public ServicioPedido(PedidoRepository pedidos) {
        this.pedidos = pedidos;
    }

    public Pedido crear(int clienteId, List<LineaPedido> lineas) {
        return pedidos.crear(clienteId, lineas);
    }
}
