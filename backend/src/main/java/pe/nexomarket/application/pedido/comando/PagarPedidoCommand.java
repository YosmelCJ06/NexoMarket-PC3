package pe.nexomarket.application.pedido.comando;

import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.memento.HistorialPedidoCaretaker;

/** ConcreteCommand: registra el pago del pedido. */
public class PagarPedidoCommand extends CambioEstadoPedidoCommand {
    public PagarPedidoCommand(Pedido pedido, HistorialPedidoCaretaker historial) {
        super(pedido, historial);
    }

    @Override
    protected void aplicar(Pedido pedido) {
        pedido.pagar();
    }

    @Override
    protected String nombreAccion() {
        return "Pagar";
    }
}
