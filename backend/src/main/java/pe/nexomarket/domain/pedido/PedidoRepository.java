package pe.nexomarket.domain.pedido;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de pedidos. El dominio y los patrones dependen de
 * esta interfaz, no de PostgreSQL (principio de inversión de dependencias).
 */
public interface PedidoRepository {
    Optional<Pedido> buscarPorId(int pedidoId);

    List<Pedido> listar();

    /** Guarda el estado y el monto actuales del pedido. */
    void guardar(Pedido pedido);

    /** Correo del cliente, usado por el observador que le notifica los cambios. */
    Optional<String> buscarCorreoCliente(int clienteId);
}
