package pe.nexomarket.domain.pedido;

/** ConcreteState final: el pedido llegó al cliente. No admite más acciones. */
public class EstadoEntregado extends EstadoBase {
    public static final String CODIGO = "ENTREGADO";

    @Override
    public String getNombreEstado() {
        return CODIGO;
    }

    @Override
    public boolean esFinal() {
        return true;
    }
}
