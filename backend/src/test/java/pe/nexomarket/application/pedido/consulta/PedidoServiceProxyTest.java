package pe.nexomarket.application.pedido.consulta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.PedidoNoEncontradoException;
import pe.nexomarket.domain.seguridad.AccesoDenegadoException;
import pe.nexomarket.domain.seguridad.Rol;
import pe.nexomarket.domain.seguridad.UsuarioSesion;
import pe.nexomarket.infrastructure.memoria.PedidoRepositoryEnMemoria;

class PedidoServiceProxyTest {
    private PedidoRepositoryEnMemoria repositorio;

    @BeforeEach
    void preparar() {
        repositorio = new PedidoRepositoryEnMemoria();
        repositorio.guardar(new Pedido(5001, 102, new BigDecimal("7499.90")));
    }

    private PedidoServiceProxy proxyPara(UsuarioSesion usuario) {
        return new PedidoServiceProxy(() -> new PedidoRealService(repositorio), usuario);
    }

    @Test
    void unInvitadoNoPuedeConsultarNiCargaElServicioReal() {
        PedidoServiceProxy proxy = proxyPara(UsuarioSesion.invitado());
        assertThrows(AccesoDenegadoException.class, () -> proxy.obtenerDetallePedido(5001));
        assertFalse(proxy.servicioRealCreado());
    }

    @Test
    void unClienteVeSoloSusPedidos() {
        PedidoServiceProxy duenio = proxyPara(new UsuarioSesion(10, 102, Rol.CLIENTE));
        assertEquals("PED-05001", duenio.obtenerDetallePedido(5001).codigo());

        PedidoServiceProxy otroCliente = proxyPara(new UsuarioSesion(11, 999, Rol.CLIENTE));
        assertThrows(AccesoDenegadoException.class, () -> otroCliente.obtenerDetallePedido(5001));
    }

    @Test
    void elAdministradorVeCualquierPedidoYElServicioSeCreaAlUsarlo() {
        PedidoServiceProxy proxy = proxyPara(new UsuarioSesion(1, null, Rol.ADMINISTRADOR));
        assertFalse(proxy.servicioRealCreado());

        DetallePedido detalle = proxy.obtenerDetallePedido(5001);

        assertTrue(proxy.servicioRealCreado());
        assertEquals("PENDIENTE", detalle.estado());
        assertEquals(new BigDecimal("7499.90"), detalle.montoTotal());
    }

    @Test
    void informaCuandoElPedidoNoExiste() {
        PedidoServiceProxy proxy = proxyPara(new UsuarioSesion(1, null, Rol.ADMINISTRADOR));
        assertThrows(PedidoNoEncontradoException.class, () -> proxy.obtenerDetallePedido(1));
    }

    @Test
    void unRolDesconocidoSeTrataComoInvitado() {
        assertEquals(Rol.INVITADO, Rol.desdeTexto("ADMIN"));
        assertEquals(Rol.ADMINISTRADOR, Rol.desdeTexto("administrador"));
    }
}
