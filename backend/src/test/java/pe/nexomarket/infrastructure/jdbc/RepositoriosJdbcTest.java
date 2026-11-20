package pe.nexomarket.infrastructure.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import pe.nexomarket.domain.catalogo.CategoriaComposite;
import pe.nexomarket.domain.pedido.LineaPedido;
import pe.nexomarket.domain.pedido.Pedido;

/**
 * Ejecuta database/schema.sql y database/datos_demo.sql sobre H2 en modo
 * PostgreSQL y prueba el SQL real de los repositorios JDBC.
 */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:nexomarket;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="})
@Sql({"file:../database/schema.sql", "file:../database/datos_demo.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import({PedidoRepositoryJdbc.class, CatalogoRepositoryJdbc.class, ProductoRepositoryJdbc.class})
class RepositoriosJdbcTest {
    @Autowired
    private PedidoRepositoryJdbc pedidos;
    @Autowired
    private CatalogoRepositoryJdbc catalogo;
    @Autowired
    private ProductoRepositoryJdbc productos;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void leeLosPedidosDeDemostracionConSuEstado() {
        Pedido pedido = pedidos.buscarPorId(1).orElseThrow();
        assertEquals("PENDIENTE", pedido.getNombreEstado());
        assertEquals(new BigDecimal("7889.80"), pedido.getMontoTotal());
        assertEquals(3, pedidos.listar().size());
        assertTrue(pedidos.buscarPorId(99).isEmpty());
        assertEquals("ana@nexomarket.pe", pedidos.buscarCorreoCliente(pedido.getClienteId()).orElseThrow());
    }

    @Test
    void guardaElNuevoEstadoDelPedido() {
        Pedido pedido = pedidos.buscarPorId(1).orElseThrow();
        pedido.pagar();
        pedidos.guardar(pedido);
        assertEquals("PAGADO", pedidos.buscarPorId(1).orElseThrow().getNombreEstado());
    }

    @Test
    void creaUnPedidoConSusLineas() {
        int clienteId = jdbc.queryForObject("SELECT id_cliente FROM cliente WHERE email = 'luis@nexomarket.pe'", Integer.class);
        Pedido nuevo = pedidos.crear(clienteId, List.of(
                new LineaPedido(1, 2, new BigDecimal("7499.90")),
                new LineaPedido(6, 1, new BigDecimal("389.90"))));

        assertEquals("PENDIENTE", nuevo.getNombreEstado());
        assertEquals(new BigDecimal("15389.70"), nuevo.getMontoTotal());
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM detalle_pedido WHERE id_pedido = ?",
                Integer.class, nuevo.getId()));
    }

    @Test
    void descuentaStockSoloSiAlcanza() {
        assertTrue(productos.descontarStock(4, 3));
        assertEquals(1, productos.buscarPorId(4).orElseThrow().getStock());
        assertFalse(productos.descontarStock(4, 2));
        assertEquals(1, productos.buscarPorId(4).orElseThrow().getStock());
    }

    @Test
    void armaElCatalogoComoArbolComposite() {
        CategoriaComposite raiz = catalogo.cargarCatalogo();
        assertEquals(4, raiz.getHijos().size());
        assertEquals(6, raiz.contarProductos());
        assertEquals(79, raiz.obtenerStockTotal());
    }

    @Test
    void laBaseRechazaUnEstadoQueNoExiste() {
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE pedido SET estado = 'PERDIDO' WHERE id_pedido = 1"));
    }
}
