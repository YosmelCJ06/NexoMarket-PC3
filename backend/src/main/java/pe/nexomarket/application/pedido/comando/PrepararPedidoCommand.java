package pe.nexomarket.application.pedido.comando;

import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.memento.HistorialPedidoCaretaker;

/** ConcreteCommand: pasa el pedido a preparación en almacén. */
public class PrepararPedidoCommand extends CambioEstadoPedidoCommand {
    public PrepararPedidoCommand(Pedido pedido, HistorialPedidoCaretaker historial) {
        super(pedido, historial);
    }

    @Override
    protected void aplicar(Pedido pedido) {
        pedido.preparar();
    }

    @Override
    protected String nombreAccion() {
        return "Preparar";
    }
}
