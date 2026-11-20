package pe.nexomarket.application.pedido.comando;

import java.util.Objects;
import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.PedidoMemento;
import pe.nexomarket.domain.pedido.memento.HistorialPedidoCaretaker;

/**
 * Base de los comandos que cambian el estado de un pedido (Receiver = Pedido).
 * Antes de ejecutar toma un Memento; si la acción es válida lo entrega al
 * Caretaker, y {@link #deshacer()} lo usa para volver al estado anterior.
 */
public abstract class CambioEstadoPedidoCommand implements Comando {
    private final Pedido pedido;
    private final HistorialPedidoCaretaker historial;

    protected CambioEstadoPedidoCommand(Pedido pedido, HistorialPedidoCaretaker historial) {
        this.pedido = Objects.requireNonNull(pedido);
        this.historial = Objects.requireNonNull(historial);
    }

    @Override
    public final void ejecutar() {
        PedidoMemento antes = pedido.crearMemento();
        aplicar(pedido);
        historial.guardar(pedido.getId(), antes);
    }

    @Override
    public final void deshacer() {
        pedido.restaurar(historial.deshacer(pedido.getId()));
    }

    @Override
    public String descripcion() {
        return nombreAccion() + " " + pedido.getCodigo();
    }

    public int pedidoId() {
        return pedido.getId();
    }

    /** Acción concreta que se delega al Receiver (el Pedido y su State). */
    protected abstract void aplicar(Pedido pedido);

    protected abstract String nombreAccion();
}
