package pe.nexomarket.domain.pedido;

/** ConcreteState final: el pedido fue anulado. No admite más acciones. */
public class EstadoCancelado extends EstadoBase {
    public static final String CODIGO = "CANCELADO";

    @Override
    public String getNombreEstado() {
        return CODIGO;
    }

    @Override
    public boolean esFinal() {
        return true;
    }
}
