package pe.nexomarket.application.pedido;

import java.util.List;
import org.springframework.stereotype.Service;
import pe.nexomarket.application.pedido.comando.AccionPedido;
import pe.nexomarket.application.pedido.comando.CambioEstadoPedidoCommand;
import pe.nexomarket.application.pedido.comando.Comando;
import pe.nexomarket.application.pedido.comando.InvocadorComandos;
import pe.nexomarket.application.pedido.comando.InvocadorComandos.RegistroComando;
import pe.nexomarket.application.pedido.consulta.DetallePedido;
import pe.nexomarket.application.pedido.consulta.IPedidoService;
import pe.nexomarket.application.pedido.consulta.PedidoRealService;
import pe.nexomarket.application.pedido.consulta.PedidoServiceProxy;
import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.PedidoNoEncontradoException;
import pe.nexomarket.domain.pedido.PedidoRepository;
import pe.nexomarket.domain.pedido.memento.HistorialPedidoCaretaker;
import pe.nexomarket.domain.pedido.observer.AuditoriaPedidoObservador;
import pe.nexomarket.domain.pedido.observer.EventoPedido;
import pe.nexomarket.domain.pedido.observer.NotificacionClienteObservador;
import pe.nexomarket.domain.seguridad.AccesoDenegadoException;
import pe.nexomarket.domain.seguridad.UsuarioSesion;

/**
 * Casos de uso del pedido que usan los patrones de la PC3:
 * consulta protegida (Proxy), acciones reversibles (Command + Memento + State),
 * avisos de cambio (Observer) e historial para auditoría.
 */
@Service
public class PedidoAccionService {
    private final PedidoRepository repositorio;
    private final InvocadorComandos invocador;
    private final HistorialPedidoCaretaker historial;
    private final AuditoriaPedidoObservador auditoria;

    public PedidoAccionService(PedidoRepository repositorio, InvocadorComandos invocador,
            HistorialPedidoCaretaker historial, AuditoriaPedidoObservador auditoria) {
        this.repositorio = repositorio;
        this.invocador = invocador;
        this.historial = historial;
        this.auditoria = auditoria;
    }

    /** PROXY: la consulta pasa por PedidoServiceProxy, que valida el rol y el dueño del pedido. */
    public DetallePedido consultar(int pedidoId, UsuarioSesion usuario) {
        IPedidoService servicio = new PedidoServiceProxy(() -> new PedidoRealService(repositorio), usuario);
        return servicio.obtenerDetallePedido(pedidoId);
    }

    /** COMMAND: ejecuta la acción como comando reversible y guarda el nuevo estado. */
    public DetallePedido ejecutarAccion(int pedidoId, String accion, UsuarioSesion usuario) {
        exigirAdministrador(usuario);
        AccionPedido tipo = AccionPedido.desdeTexto(accion);
        Pedido pedido = repositorio.buscarPorId(pedidoId).orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));
        suscribirObservadores(pedido);
        invocador.ejecutarComando(tipo.crearComando(pedido, historial));
        try {
            repositorio.guardar(pedido);
        } catch (RuntimeException errorAlGuardar) {
            // Si la base rechaza el cambio, se revierte el comando para no dejar memoria y BD distintas.
            invocador.deshacerUltimo(c -> c instanceof CambioEstadoPedidoCommand cambio && cambio.pedidoId() == pedidoId);
            throw errorAlGuardar;
        }
        return DetallePedido.desde(pedido);
    }

    /** MEMENTO: deshace el último comando de este pedido y guarda el estado restaurado. */
    public DetallePedido deshacer(int pedidoId, UsuarioSesion usuario) {
        exigirAdministrador(usuario);
        Comando comando = invocador.deshacerUltimo(c -> c instanceof CambioEstadoPedidoCommand cambio
                && cambio.pedidoId() == pedidoId);
        Pedido pedido = ((CambioEstadoPedidoCommand) comando).pedido();
        repositorio.guardar(pedido);
        return DetallePedido.desde(pedido);
    }

    /** OBSERVER + COMMAND: eventos de auditoría y comandos ejecutados o deshechos del pedido. */
    public HistorialPedido historial(int pedidoId, UsuarioSesion usuario) {
        DetallePedido detalle = consultar(pedidoId, usuario);
        List<RegistroComando> comandos = invocador.getRegistro().stream()
                .filter(registro -> registro.descripcion().endsWith(" " + detalle.codigo()))
                .toList();
        return new HistorialPedido(detalle, auditoria.getRegistros(pedidoId), comandos,
                historial.puedeDeshacer(pedidoId));
    }

    private void suscribirObservadores(Pedido pedido) {
        pedido.suscribir(auditoria);
        String correo = repositorio.buscarCorreoCliente(pedido.getClienteId())
                .orElse("cliente-" + pedido.getClienteId());
        pedido.suscribir(new NotificacionClienteObservador(correo));
    }

    private static void exigirAdministrador(UsuarioSesion usuario) {
        if (usuario == null || !usuario.esAdministrador()) {
            throw new AccesoDenegadoException("Se requiere una sesión de administrador.");
        }
    }

    public record HistorialPedido(DetallePedido pedido, List<EventoPedido> eventos,
            List<RegistroComando> comandos, boolean puedeDeshacer) {
    }
}
