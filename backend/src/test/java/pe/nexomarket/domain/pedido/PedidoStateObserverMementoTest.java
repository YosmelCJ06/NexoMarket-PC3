package pe.nexomarket.domain.pedido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.nexomarket.domain.pedido.memento.HistorialPedidoCaretaker;
import pe.nexomarket.domain.pedido.observer.AuditoriaPedidoObservador;
import pe.nexomarket.domain.pedido.observer.EventoPedido;
import pe.nexomarket.domain.pedido.observer.NotificacionClienteObservador;

class PedidoStateObserverMementoTest {
    private Pedido pedido;
    private AuditoriaPedidoObservador auditoria;
    private NotificacionClienteObservador notificacion;

    @BeforeEach
    void prepararPedido() {
        pedido = new Pedido(5001, 102, new BigDecimal("9898.90"));
        auditoria = new AuditoriaPedidoObservador();
        notificacion = new NotificacionClienteObservador("cliente@nexomarket.pe");
        pedido.suscribir(auditoria);
        pedido.suscribir(notificacion);
    }

    // ---------- STATE ----------

    @Test
    void recorreElCicloDeVidaCompleto() {
        assertEquals("PENDIENTE", pedido.getNombreEstado());
        pedido.pagar();
        assertEquals("PAGADO", pedido.getNombreEstado());
        pedido.preparar();
        assertEquals("PREPARANDO", pedido.getNombreEstado());
        pedido.enviar();
        assertEquals("ENVIADO", pedido.getNombreEstado());
        pedido.entregar();
        assertEquals("ENTREGADO", pedido.getNombreEstado());
        assertTrue(pedido.estaFinalizado());
    }

    @Test
    void rechazaTransicionesInvalidas() {
        TransicionInvalidaException error = assertThrows(TransicionInvalidaException.class, pedido::enviar);
        assertEquals("PENDIENTE", error.getEstadoActual());
        assertEquals("PENDIENTE", pedido.getNombreEstado());

        pedido.pagar();
        pedido.preparar();
        pedido.enviar();
        assertThrows(TransicionInvalidaException.class, pedido::cancelar);
    }

    @Test
    void unPedidoCanceladoNoAdmiteMasAcciones() {
        pedido.cancelar();
        assertEquals("CANCELADO", pedido.getNombreEstado());
        assertThrows(TransicionInvalidaException.class, pedido::pagar);
        assertThrows(TransicionInvalidaException.class, pedido::cancelar);
    }

    @Test
    void reconstruyeElEstadoGuardadoEnBaseDeDatos() {
        Pedido guardado = Pedido.reconstruir(7, 3, BigDecimal.TEN, "preparando");
        assertEquals("PREPARANDO", guardado.getNombreEstado());
        guardado.enviar();
        assertEquals("ENVIADO", guardado.getNombreEstado());
        assertThrows(IllegalArgumentException.class, () -> Pedido.reconstruir(7, 3, BigDecimal.TEN, "PERDIDO"));
    }

    // ---------- OBSERVER ----------

    @Test
    void notificaATodosLosObservadoresEnCadaCambio() {
        pedido.pagar();
        pedido.preparar();

        List<EventoPedido> eventos = auditoria.getRegistros(5001);
        assertEquals(2, eventos.size());
        assertEquals("PENDIENTE", eventos.get(0).estadoAnterior());
        assertEquals("PAGADO", eventos.get(0).estadoNuevo());
        assertEquals("PREPARANDO", eventos.get(1).estadoNuevo());
        assertTrue(notificacion.getUltimoMensaje().contains("PED-05001 ahora está PREPARANDO"));
    }

    @Test
    void unaTransicionRechazadaNoNotifica() {
        assertThrows(TransicionInvalidaException.class, pedido::entregar);
        assertTrue(auditoria.getRegistros().isEmpty());
    }

    @Test
    void unObservadorDesuscritoDejaDeRecibirAvisos() {
        pedido.desuscribir(auditoria);
        pedido.pagar();
        assertTrue(auditoria.getRegistros().isEmpty());
    }

    // ---------- MEMENTO ----------

    @Test
    void restauraElEstadoAnteriorConElCaretaker() {
        HistorialPedidoCaretaker historial = new HistorialPedidoCaretaker();

        historial.guardar(pedido.getId(), pedido.crearMemento());
        pedido.pagar();
        historial.guardar(pedido.getId(), pedido.crearMemento());
        pedido.preparar();
        assertEquals("PREPARANDO", pedido.getNombreEstado());

        pedido.restaurar(historial.deshacer(pedido.getId()));
        assertEquals("PAGADO", pedido.getNombreEstado());
        pedido.restaurar(historial.deshacer(pedido.getId()));
        assertEquals("PENDIENTE", pedido.getNombreEstado());
        assertFalse(historial.puedeDeshacer(pedido.getId()));

        EventoPedido ultimo = auditoria.getRegistros().getLast();
        assertTrue(ultimo.detalle().contains("Memento"));
        assertThrows(IllegalStateException.class, () -> historial.deshacer(pedido.getId()));
    }

    @Test
    void noRestauraUnMementoDeOtroPedido() {
        Pedido otro = new Pedido(9, 1, BigDecimal.ONE);
        PedidoMemento ajeno = otro.crearMemento();
        assertThrows(IllegalArgumentException.class, () -> pedido.restaurar(ajeno));
    }
}
