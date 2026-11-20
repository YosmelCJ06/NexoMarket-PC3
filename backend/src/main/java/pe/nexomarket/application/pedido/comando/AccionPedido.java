package pe.nexomarket.application.pedido.comando;

import java.util.Locale;
import java.util.function.BiFunction;
import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.memento.HistorialPedidoCaretaker;

/**
 * Catálogo de acciones que la API acepta. Cada acción sabe qué comando crear,
 * así el controlador no necesita un switch para elegirlo.
 */
public enum AccionPedido {
    PAGAR(PagarPedidoCommand::new),
    PREPARAR(PrepararPedidoCommand::new),
    ENVIAR(EnviarPedidoCommand::new),
    ENTREGAR(EntregarPedidoCommand::new),
    CANCELAR(CancelarPedidoCommand::new);

    private final BiFunction<Pedido, HistorialPedidoCaretaker, CambioEstadoPedidoCommand> fabrica;

    AccionPedido(BiFunction<Pedido, HistorialPedidoCaretaker, CambioEstadoPedidoCommand> fabrica) {
        this.fabrica = fabrica;
    }

    public CambioEstadoPedidoCommand crearComando(Pedido pedido, HistorialPedidoCaretaker historial) {
        return fabrica.apply(pedido, historial);
    }

    public static AccionPedido desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) throw new IllegalArgumentException("La acción es obligatoria.");
        try {
            return valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Acción de pedido desconocida: " + texto);
        }
    }
}
