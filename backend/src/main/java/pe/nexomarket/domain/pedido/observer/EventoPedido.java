package pe.nexomarket.domain.pedido.observer;

import java.time.Instant;

/** Datos que el Subject (Pedido) envía a cada observador cuando cambia de estado. */
public record EventoPedido(
        int pedidoId,
        String codigoPedido,
        String estadoAnterior,
        String estadoNuevo,
        String detalle,
        Instant fecha) {

    public EventoPedido(int pedidoId, String codigoPedido, String estadoAnterior, String estadoNuevo, String detalle) {
        this(pedidoId, codigoPedido, estadoAnterior, estadoNuevo, detalle, Instant.now());
    }
}
