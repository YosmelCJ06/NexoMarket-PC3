package pe.nexomarket.application.pedido.comando;

import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.memento.HistorialPedidoCaretaker;

/** ConcreteCommand: despacha el pedido al courier. */
public class EnviarPedidoCommand extends CambioEstadoPedidoCommand {
    public EnviarPedidoCommand(Pedido pedido, HistorialPedidoCaretaker historial) {
        super(pedido, historial);
    }

    @Override
    protected void aplicar(Pedido pedido) {
        pedido.enviar();
    }

    @Override
    protected String nombreAccion() {
        return "Enviar";
    }
}
