package pe.nexomarket.application.pedido.comando;

import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.memento.HistorialPedidoCaretaker;

/** ConcreteCommand: anula el pedido (si su estado lo permite). */
public class CancelarPedidoCommand extends CambioEstadoPedidoCommand {
    public CancelarPedidoCommand(Pedido pedido, HistorialPedidoCaretaker historial) {
        super(pedido, historial);
    }

    @Override
    protected void aplicar(Pedido pedido) {
        pedido.cancelar();
    }

    @Override
    protected String nombreAccion() {
        return "Cancelar";
    }
}
