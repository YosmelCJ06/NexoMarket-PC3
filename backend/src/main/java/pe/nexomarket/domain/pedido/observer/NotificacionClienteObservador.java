package pe.nexomarket.domain.pedido.observer;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConcreteObserver: avisa al cliente que su pedido cambió de estado.
 * El envío es simulado (se registra en el log); no se manda un correo real.
 */
public class NotificacionClienteObservador implements ObservadorPedido {
    private static final Logger log = LoggerFactory.getLogger(NotificacionClienteObservador.class);
    private final String correoCliente;
    private String ultimoMensaje;

    public NotificacionClienteObservador(String correoCliente) {
        this.correoCliente = Objects.requireNonNull(correoCliente);
    }

    @Override
    public void actualizar(EventoPedido evento) {
        ultimoMensaje = "Tu pedido " + evento.codigoPedido() + " ahora está " + evento.estadoNuevo()
                + ": " + evento.detalle();
        log.info("[EMAIL SIMULADO -> {}] {}", correoCliente, ultimoMensaje);
    }

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }

    /** Dos observadores que notifican al mismo correo se consideran el mismo suscriptor. */
    @Override
    public boolean equals(Object otro) {
        return otro instanceof NotificacionClienteObservador o && correoCliente.equals(o.correoCliente);
    }

    @Override
    public int hashCode() {
        return correoCliente.hashCode();
    }
}
