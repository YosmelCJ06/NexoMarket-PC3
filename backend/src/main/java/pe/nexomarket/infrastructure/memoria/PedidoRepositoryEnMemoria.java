package pe.nexomarket.infrastructure.memoria;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.PedidoRepository;

/**
 * Implementación temporal en memoria. Permite probar los patrones sin base de
 * datos; luego se reemplaza por una versión JDBC sin tocar el dominio.
 */
public class PedidoRepositoryEnMemoria implements PedidoRepository {
    private final Map<Integer, Pedido> pedidos = new ConcurrentHashMap<>();

    @Override
    public Optional<Pedido> buscarPorId(int pedidoId) {
        return Optional.ofNullable(pedidos.get(pedidoId));
    }

    @Override
    public List<Pedido> listar() {
        return pedidos.values().stream().sorted(Comparator.comparingInt(Pedido::getId)).toList();
    }

    @Override
    public void guardar(Pedido pedido) {
        pedidos.put(pedido.getId(), pedido);
    }
}
