package pe.nexomarket.domain.pedido.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConcreteObserver: registra cada cambio de estado para auditoría.
 * Guarda los eventos en memoria para poder consultarlos desde la API.
 */
public class AuditoriaPedidoObservador implements ObservadorPedido {
    private static final Logger log = LoggerFactory.getLogger(AuditoriaPedidoObservador.class);
    private final List<EventoPedido> registros = new CopyOnWriteArrayList<>();

    @Override
    public void actualizar(EventoPedido evento) {
        registros.add(evento);
        log.info("[AUDITORIA] {}: {} -> {} | {}", evento.codigoPedido(), evento.estadoAnterior(),
                evento.estadoNuevo(), evento.detalle());
    }

    public List<EventoPedido> getRegistros() {
        return List.copyOf(registros);
    }

    public List<EventoPedido> getRegistros(int pedidoId) {
        return registros.stream().filter(evento -> evento.pedidoId() == pedidoId).toList();
    }
}
