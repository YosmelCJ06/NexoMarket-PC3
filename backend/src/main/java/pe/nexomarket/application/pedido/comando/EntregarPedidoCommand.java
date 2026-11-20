package pe.nexomarket.application.pedido.comando;

import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.memento.HistorialPedidoCaretaker;

/** ConcreteCommand: confirma la entrega al cliente. */
public class EntregarPedidoCommand extends CambioEstadoPedidoCommand {
    public EntregarPedidoCommand(Pedido pedido, HistorialPedidoCaretaker historial) {
        super(pedido, historial);
    }

    @Override
    protected void aplicar(Pedido pedido) {
        pedido.entregar();
    }

    @Override
    protected String nombreAccion() {
        return "Entregar";
    }
}
