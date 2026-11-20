package pe.nexomarket.domain.pedido.observer;

/** Patrón OBSERVER — participante "Observer": recibe el aviso de cada cambio de estado. */
public interface ObservadorPedido {
    void actualizar(EventoPedido evento);
}
