package pe.nexomarket.domain.pedido;

/** ConcreteState: pedido creado, a la espera del pago. */
public class EstadoPendiente extends EstadoBase {
    public static final String CODIGO = "PENDIENTE";

    @Override
    public void pagar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoPagado(), "Pago registrado correctamente.");
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoCancelado(), "Pedido cancelado antes del pago.");
    }

    @Override
    public String getNombreEstado() {
        return CODIGO;
    }
}
