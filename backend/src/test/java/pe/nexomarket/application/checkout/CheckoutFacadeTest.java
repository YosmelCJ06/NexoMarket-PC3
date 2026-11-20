package pe.nexomarket.application.checkout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.nexomarket.application.pedido.PedidoAccionService;
import pe.nexomarket.application.pedido.comando.InvocadorComandos;
import pe.nexomarket.application.pedido.consulta.DetallePedido;
import pe.nexomarket.domain.catalogo.ProductoHoja;
import pe.nexomarket.domain.catalogo.ProductoRepository;
import pe.nexomarket.domain.catalogo.StockInsuficienteException;
import pe.nexomarket.domain.pedido.memento.HistorialPedidoCaretaker;
import pe.nexomarket.domain.pedido.observer.AuditoriaPedidoObservador;
import pe.nexomarket.domain.seguridad.AccesoDenegadoException;
import pe.nexomarket.domain.seguridad.Rol;
import pe.nexomarket.domain.seguridad.UsuarioSesion;
import pe.nexomarket.infrastructure.memoria.PedidoRepositoryEnMemoria;

class CheckoutFacadeTest {
    private static final UsuarioSesion ANA = new UsuarioSesion(10, 102, Rol.CLIENTE);

    private final Map<Integer, ProductoHoja> stock = new HashMap<>();
    private PedidoRepositoryEnMemoria pedidos;
    private AuditoriaPedidoObservador auditoria;
    private CheckoutFacade checkout;

    @BeforeEach
    void preparar() {
        stock.put(1, new ProductoHoja(1, "MacBook Pro 14", new BigDecimal("7499.90"), 2));
        stock.put(2, new ProductoHoja(2, "Mouse Logitech", new BigDecimal("389.90"), 30));
        ProductoRepository productos = new ProductoRepository() {
            @Override
            public Optional<ProductoHoja> buscarPorId(int id) {
                return Optional.ofNullable(stock.get(id));
            }

            @Override
            public boolean descontarStock(int id, int cantidad) {
                ProductoHoja actual = stock.get(id);
                if (actual.getStock() < cantidad) return false;
                stock.put(id, new ProductoHoja(id, actual.obtenerNombre(), actual.getPrecio(), actual.getStock() - cantidad));
                return true;
            }
        };
        pedidos = new PedidoRepositoryEnMemoria();
        auditoria = new AuditoriaPedidoObservador();
        PedidoAccionService acciones = new PedidoAccionService(pedidos, new InvocadorComandos(),
                new HistorialPedidoCaretaker(), auditoria);
        checkout = new CheckoutFacade(new ServicioInventario(productos), new ServicioPago(),
                new ServicioPedido(pedidos), acciones);
    }

    @Test
    void unaCompraGuardaElPedidoDescuentaStockYQuedaPagada() {
        DetallePedido pedido = checkout.comprar(ANA,
                List.of(new ItemCarrito(1, 1), new ItemCarrito(2, 2), new ItemCarrito(2, 1)), "yape");

        assertEquals("PAGADO", pedido.estado());
        assertEquals(new BigDecimal("8669.60"), pedido.montoTotal());
        assertEquals(1, stock.get(1).getStock());
        assertEquals(27, stock.get(2).getStock());
        assertEquals(2, pedidos.lineasDe(pedido.id()).size());
        assertEquals("PAGADO", auditoria.getRegistros(pedido.id()).getFirst().estadoNuevo());
    }

    @Test
    void rechazaLaCompraSiNoHayStock() {
        assertThrows(StockInsuficienteException.class,
                () -> checkout.comprar(ANA, List.of(new ItemCarrito(1, 5)), "TARJETA"));
        assertEquals(0, pedidos.listar().size());
    }

    @Test
    void validaElMetodoDePagoYElCarrito() {
        assertThrows(IllegalArgumentException.class,
                () -> checkout.comprar(ANA, List.of(new ItemCarrito(2, 1)), "BITCOIN"));
        assertThrows(IllegalArgumentException.class, () -> checkout.comprar(ANA, List.of(), "YAPE"));
        assertThrows(IllegalArgumentException.class,
                () -> checkout.comprar(ANA, List.of(new ItemCarrito(9, 1)), "YAPE"));
    }

    @Test
    void soloUnClienteConSesionPuedeComprar() {
        assertThrows(AccesoDenegadoException.class,
                () -> checkout.comprar(UsuarioSesion.invitado(), List.of(new ItemCarrito(2, 1)), "YAPE"));
        assertThrows(AccesoDenegadoException.class,
                () -> checkout.comprar(new UsuarioSesion(1, null, Rol.ADMINISTRADOR), List.of(new ItemCarrito(2, 1)), "YAPE"));
    }
}
