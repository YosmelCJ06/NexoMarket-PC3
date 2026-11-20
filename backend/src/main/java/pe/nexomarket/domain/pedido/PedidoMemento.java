package pe.nexomarket.domain.pedido;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Patrón MEMENTO — participante "Memento".
 * Guarda una foto del estado interno del pedido. El constructor y los datos
 * guardados son de paquete: solo {@link Pedido} (Originator) puede crearlo y
 * leerlo. El Caretaker solo lo almacena y ve la fecha, sin acceder al contenido.
 */
public final class PedidoMemento {
    private final int pedidoId;
    private final EstadoPedido estado;
    private final BigDecimal montoTotal;
    private final Instant fechaCaptura;

    PedidoMemento(int pedidoId, EstadoPedido estado, BigDecimal montoTotal) {
        this.pedidoId = pedidoId;
        this.estado = estado;
        this.montoTotal = montoTotal;
        this.fechaCaptura = Instant.now();
    }

    int pedidoId() {
        return pedidoId;
    }

    EstadoPedido estado() {
        return estado;
    }

    BigDecimal montoTotal() {
        return montoTotal;
    }

    /** Dato no sensible que el Caretaker puede mostrar en un historial. */
    public Instant getFechaCaptura() {
        return fechaCaptura;
    }
}
