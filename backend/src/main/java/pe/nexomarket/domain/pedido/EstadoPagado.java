package pe.nexomarket.domain.pedido;

/** ConcreteState: pago confirmado, listo para preparar. */
public class EstadoPagado extends EstadoBase {
    public static final String CODIGO = "PAGADO";

    @Override
    public void preparar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoEnPreparacion(), "El almacén empezó a preparar el pedido.");
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoCancelado(), "Pedido pagado cancelado; procede el reembolso.");
    }

    @Override
    public String getNombreEstado() {
        return CODIGO;
    }
}
