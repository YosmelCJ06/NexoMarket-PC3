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

    void guardar(Pedido pedido);
}
