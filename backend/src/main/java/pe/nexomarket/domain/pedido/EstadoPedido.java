package pe.nexomarket.domain.pedido;

/**
 * Patrón STATE — participante "State".
 * Declara las acciones que dependen del estado del pedido. Cada estado concreto
 * decide si la acción es válida y a qué estado pasa el {@link Pedido} (Context).
 */
public interface EstadoPedido {
    void pagar(Pedido pedido);

    void preparar(Pedido pedido);

    void enviar(Pedido pedido);

    void entregar(Pedido pedido);

    void cancelar(Pedido pedido);

    String getNombreEstado();

    default boolean esFinal() {
        return false;
    }
}
