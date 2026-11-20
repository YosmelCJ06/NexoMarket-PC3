package pe.nexomarket.domain.pedido;

/** ConcreteState: el pedido está en camino; ya no se puede cancelar. */
public class EstadoEnviado extends EstadoBase {
    public static final String CODIGO = "ENVIADO";

    @Override
    public void entregar(Pedido pedido) {
        pedido.cambiarEstado(new EstadoEntregado(), "Pedido entregado al cliente.");
    }

    @Override
    public String getNombreEstado() {
        return CODIGO;
    }
}
