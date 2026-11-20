package pe.nexomarket.application.pedido.consulta;

import java.util.Objects;
import pe.nexomarket.domain.pedido.PedidoNoEncontradoException;
import pe.nexomarket.domain.pedido.PedidoRepository;

/**
 * Patrón PROXY — participante "RealSubject" (el "ObjetoReal" del diagrama del curso).
 * Hace la consulta real al repositorio, sin ningún control de acceso.
 */
public class PedidoRealService implements IPedidoService {
    private final PedidoRepository repositorio;

    public PedidoRealService(PedidoRepository repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    @Override
    public DetallePedido obtenerDetallePedido(int pedidoId) {
        return repositorio.buscarPorId(pedidoId)
                .map(DetallePedido::desde)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));
    }
}
