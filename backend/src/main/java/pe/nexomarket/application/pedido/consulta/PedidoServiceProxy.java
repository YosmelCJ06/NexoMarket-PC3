package pe.nexomarket.application.pedido.consulta;

import java.util.Objects;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.nexomarket.domain.seguridad.AccesoDenegadoException;
import pe.nexomarket.domain.seguridad.UsuarioSesion;

/**
 * Patrón PROXY — participante "Proxy". Combina tres tipos vistos en clase:
 * <ul>
 *   <li>Protection Proxy: un invitado no puede consultar pedidos y un cliente
 *       solo puede ver los suyos.</li>
 *   <li>Virtual Proxy: el servicio real se crea recién en la primera consulta
 *       permitida (carga diferida).</li>
 *   <li>Smart Proxy: registra en el log cada acceso permitido o denegado.</li>
 * </ul>
 */
public class PedidoServiceProxy implements IPedidoService {
    private static final Logger log = LoggerFactory.getLogger(PedidoServiceProxy.class);

    private final Supplier<IPedidoService> creadorServicioReal;
    private final UsuarioSesion usuario;
    private IPedidoService servicioReal;

    public PedidoServiceProxy(Supplier<IPedidoService> creadorServicioReal, UsuarioSesion usuario) {
        this.creadorServicioReal = Objects.requireNonNull(creadorServicioReal);
        this.usuario = usuario == null ? UsuarioSesion.invitado() : usuario;
    }

    @Override
    public DetallePedido obtenerDetallePedido(int pedidoId) {
        if (!usuario.esAdministrador() && !usuario.esCliente()) {
            throw denegar(pedidoId, "Inicia sesión para consultar pedidos.");
        }

        DetallePedido detalle = servicioReal().obtenerDetallePedido(pedidoId);

        if (usuario.esCliente() && !Objects.equals(usuario.clienteId(), detalle.clienteId())) {
            throw denegar(pedidoId, "No tienes permiso para ver este pedido.");
        }
        log.info("[PROXY] Acceso permitido a {} para {}", detalle.codigo(), usuario.rol());
        return detalle;
    }

    /** Indica si el servicio real ya fue creado (útil para demostrar la carga diferida). */
    public boolean servicioRealCreado() {
        return servicioReal != null;
    }

    private IPedidoService servicioReal() {
        if (servicioReal == null) {
            log.info("[PROXY] Creando el servicio real de pedidos (carga diferida).");
            servicioReal = creadorServicioReal.get();
        }
        return servicioReal;
    }

    private AccesoDenegadoException denegar(int pedidoId, String mensaje) {
        log.warn("[PROXY] Acceso denegado al pedido {} para {}", pedidoId, usuario.rol());
        return new AccesoDenegadoException(mensaje);
    }
}
