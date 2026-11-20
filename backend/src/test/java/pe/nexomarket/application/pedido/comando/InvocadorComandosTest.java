package pe.nexomarket.application.pedido.comando;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.nexomarket.application.pedido.comando.InvocadorComandos.RegistroComando;
import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.TransicionInvalidaException;
import pe.nexomarket.domain.pedido.memento.HistorialPedidoCaretaker;

class InvocadorComandosTest {
    private Pedido pedido;
    private HistorialPedidoCaretaker historial;
    private InvocadorComandos invocador;

    @BeforeEach
    void preparar() {
        pedido = new Pedido(5001, 102, new BigDecimal("250.00"));
        historial = new HistorialPedidoCaretaker();
        invocador = new InvocadorComandos();
    }

    @Test
    void ejecutaComandosYLosDeshaceEnOrdenInverso() {
        invocador.ejecutarComando(AccionPedido.PAGAR.crearComando(pedido, historial));
        invocador.ejecutarComando(AccionPedido.PREPARAR.crearComando(pedido, historial));
        assertEquals("PREPARANDO", pedido.getNombreEstado());
        assertEquals(2, invocador.getTotalComandosEjecutados());

        invocador.deshacerUltimo();
        assertEquals("PAGADO", pedido.getNombreEstado());
        invocador.deshacerUltimo();
        assertEquals("PENDIENTE", pedido.getNombreEstado());
        assertThrows(IllegalStateException.class, invocador::deshacerUltimo);
    }

    @Test
    void unComandoRechazadoNoQuedaEnElHistorial() {
        assertThrows(TransicionInvalidaException.class,
                () -> invocador.ejecutarComando(new EnviarPedidoCommand(pedido, historial)));
        assertEquals(0, invocador.getTotalComandosEjecutados());
        assertEquals(0, historial.cantidadGuardada(pedido.getId()));
        assertEquals("PENDIENTE", pedido.getNombreEstado());
    }

    @Test
    void deshaceSoloElUltimoComandoDelPedidoIndicado() {
        Pedido otro = new Pedido(5002, 103, BigDecimal.TEN);
        invocador.ejecutarComando(new PagarPedidoCommand(pedido, historial));
        invocador.ejecutarComando(new PagarPedidoCommand(otro, historial));

        invocador.deshacerUltimo(comando -> comando instanceof CambioEstadoPedidoCommand cambio
                && cambio.pedidoId() == 5001);

        assertEquals("PENDIENTE", pedido.getNombreEstado());
        assertEquals("PAGADO", otro.getNombreEstado());
    }

    @Test
    void registraUnLogDeAuditoria() {
        invocador.ejecutarComando(new PagarPedidoCommand(pedido, historial));
        invocador.deshacerUltimo();

        List<RegistroComando> log = invocador.getRegistro();
        assertEquals(2, log.size());
        assertEquals("Pagar PED-05001", log.get(0).descripcion());
        assertEquals(RegistroComando.Tipo.EJECUTADO, log.get(0).tipo());
        assertEquals(RegistroComando.Tipo.DESHECHO, log.get(1).tipo());
    }

    @Test
    void traduceLaAccionDeLaApiAlComandoCorrecto() {
        assertEquals(AccionPedido.CANCELAR, AccionPedido.desdeTexto(" cancelar "));
        assertThrows(IllegalArgumentException.class, () -> AccionPedido.desdeTexto("borrar"));
    }
}
