package pe.nexomarket.domain.pedido;

/**
 * Comportamiento por defecto de todos los estados: rechazar la acción.
 * Cada estado concreto sobrescribe solo las transiciones que sí permite,
 * así se evita repetir el mismo "throw" en cada clase (Copy-Paste Programming).
 */
abstract class EstadoBase implements EstadoPedido {

    @Override
    public void pagar(Pedido pedido) {
        throw rechazar("pagar");
    }

    @Override
    public void preparar(Pedido pedido) {
        throw rechazar("preparar");
    }

    @Override
    public void enviar(Pedido pedido) {
        throw rechazar("enviar");
    }

    @Override
    public void entregar(Pedido pedido) {
        throw rechazar("entregar");
    }

    @Override
    public void cancelar(Pedido pedido) {
        throw rechazar("cancelar");
    }

    private TransicionInvalidaException rechazar(String accion) {
        return new TransicionInvalidaException(getNombreEstado(), accion);
    }
}
