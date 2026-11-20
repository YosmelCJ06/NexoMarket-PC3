package pe.nexomarket.domain.pedido;

/** ConcreteState: el pedido se está empaquetando en almacén. */
public class EstadoEnPreparacion extends EstadoBase {
    public static final String CODIGO = "PREPARANDO";

    @Override
    public void enviar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoEnviado(), "Pedido entregado al courier.");
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoCancelado(), "Pedido cancelado durante la preparación; procede el reembolso.");
    }

    @Override
    public String getNombreEstado() {
        return CODIGO;
    }
}
