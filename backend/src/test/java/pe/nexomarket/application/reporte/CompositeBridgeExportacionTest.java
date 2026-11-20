package pe.nexomarket.application.reporte;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import pe.nexomarket.application.pedido.consulta.DetallePedido;
import pe.nexomarket.domain.catalogo.CategoriaComposite;
import pe.nexomarket.domain.catalogo.ProductoHoja;

class CompositeBridgeExportacionTest {

    private CategoriaComposite catalogo() {
        CategoriaComposite laptops = new CategoriaComposite("Laptops")
                .agregar(new ProductoHoja(1, "Laptop Lenovo", new BigDecimal("2399.00"), 5))
                .agregar(new ProductoHoja(2, "MacBook Pro 14", new BigDecimal("7499.90"), 2));
        CategoriaComposite accesorios = new CategoriaComposite("Accesorios")
                .agregar(new ProductoHoja(3, "Mouse \"Pro\", inalámbrico", new BigDecimal("89.90"), 30));
        return new CategoriaComposite("Tecnología").agregar(laptops).agregar(accesorios);
    }

    // ---------- COMPOSITE ----------

    @Test
    void calculaTotalesRecorriendoCategoriasAnidadas() {
        CategoriaComposite tecnologia = catalogo();
        assertEquals(3, tecnologia.contarProductos());
        assertEquals(37, tecnologia.obtenerStockTotal());
        assertEquals(new BigDecimal("9988.80"), tecnologia.obtenerPrecioTotal());
        assertEquals(3, tecnologia.obtenerProductos().size());
    }

    @Test
    void validaLosDatosDelProducto() {
        assertThrows(IllegalArgumentException.class, () -> new ProductoHoja(1, " ", BigDecimal.ONE, 1));
        assertThrows(IllegalArgumentException.class, () -> new ProductoHoja(1, "X", new BigDecimal("-1"), 1));
        assertThrows(IllegalArgumentException.class, () -> new ProductoHoja(1, "X", BigDecimal.ONE, -1));
    }

    // ---------- BRIDGE ----------

    @Test
    void elMismoReporteSeExportaEnDistintosFormatos() {
        Exportable html = new CatalogoExportable(catalogo(), new FormatoHTMLExport());
        Exportable csv = new CatalogoExportable(catalogo(), new FormatoCSVExport());

        String salidaHtml = html.generarReporte();
        assertTrue(salidaHtml.contains("<td>MacBook Pro 14</td>"));
        assertTrue(salidaHtml.contains("&quot;Pro&quot;"));
        assertEquals("catalogo-nexomarket.html", html.nombreArchivo());

        String salidaCsv = csv.generarReporte();
        assertTrue(salidaCsv.startsWith("ID,Producto,Precio (S/),Stock\r\n"));
        assertTrue(salidaCsv.contains("3,\"Mouse \"\"Pro\"\", inalámbrico\",89.90,30"));
        assertEquals("text/csv; charset=UTF-8", csv.tipoContenido());
    }

    @Test
    void unReporteNuevoUsaLosMismosFormatos() {
        List<DetallePedido> pedidos = List.of(
                new DetallePedido(1, "PED-00001", 102, new BigDecimal("250.00"), "PAGADO"),
                new DetallePedido(2, "PED-00002", 103, new BigDecimal("99.90"), "ENVIADO"));

        String csv = new PedidosExportable(pedidos, new FormatoCSVExport()).generarReporte();
        assertTrue(csv.contains("PED-00002,103,99.90,ENVIADO"));
        assertTrue(csv.contains("Monto total (S/),349.90"));

        String html = new PedidosExportable(pedidos, new FormatoHTMLExport()).generarReporte();
        assertFalse(html.isBlank());
        assertTrue(html.contains("<h1>Reporte de pedidos</h1>"));
    }
}
