package pe.nexomarket.domain.pedido;

/** Se lanza cuando el estado actual del pedido no permite la acción solicitada. */
public class TransicionInvalidaException extends IllegalStateException {
    private final String estadoActual;
    private final String accion;

    public TransicionInvalidaException(String estadoActual, String accion) {
        super("No se puede " + accion + " un pedido en estado " + estadoActual + ".");
        this.estadoActual = estadoActual;
        this.accion = accion;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public String getAccion() {
        return accion;
    }
}
