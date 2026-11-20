package pe.nexomarket.infrastructure.memoria;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import pe.nexomarket.domain.pedido.LineaPedido;
import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.PedidoRepository;

/**
 * Implementación temporal en memoria. Permite probar los patrones sin base de
 * datos; luego se reemplaza por una versión JDBC sin tocar el dominio.
 */
public class PedidoRepositoryEnMemoria implements PedidoRepository {
    private final Map<Integer, Pedido> pedidos = new ConcurrentHashMap<>();
    private final Map<Integer, String> correos = new ConcurrentHashMap<>();
    private final Map<Integer, List<LineaPedido>> lineasPorPedido = new ConcurrentHashMap<>();

    @Override
    public Optional<Pedido> buscarPorId(int pedidoId) {
        return Optional.ofNullable(pedidos.get(pedidoId));
    }

    @Override
    public List<Pedido> listar() {
        return pedidos.values().stream().sorted(Comparator.comparingInt(Pedido::getId)).toList();
    }

    @Override
    public Pedido crear(int clienteId, List<LineaPedido> lineas) {
        BigDecimal total = lineas.stream().map(LineaPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int id = pedidos.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
        Pedido pedido = new Pedido(id, clienteId, total);
        pedidos.put(id, pedido);
        lineasPorPedido.put(id, List.copyOf(lineas));
        return pedido;
    }

    public List<LineaPedido> lineasDe(int pedidoId) {
        return lineasPorPedido.getOrDefault(pedidoId, List.of());
    }

    @Override
    public void guardar(Pedido pedido) {
        pedidos.put(pedido.getId(), pedido);
    }

    @Override
    public Optional<String> buscarCorreoCliente(int clienteId) {
        return Optional.ofNullable(correos.get(clienteId));
    }

    public void registrarCorreo(int clienteId, String correo) {
        correos.put(clienteId, correo);
    }
}
