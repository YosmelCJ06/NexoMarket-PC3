package pe.nexomarket.application.reporte;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import pe.nexomarket.domain.catalogo.NodoCatalogo;

/**
 * Patrón BRIDGE — "RefinedAbstraction": reporte del catálogo.
 * Recibe el árbol del COMPOSITE y lo recorre sin saber si cada nodo es una
 * categoría o un producto.
 */
public class CatalogoExportable extends Exportable {
    private static final List<String> COLUMNAS = List.of("ID", "Producto", "Precio (S/)", "Stock");
    private final NodoCatalogo nodoRaiz;

    public CatalogoExportable(NodoCatalogo nodoRaiz, FormatoExportacion formato) {
        super(formato);
        this.nodoRaiz = Objects.requireNonNull(nodoRaiz);
    }

    @Override
    protected ReporteTabla construirReporte() {
        List<List<String>> filas = nodoRaiz.obtenerProductos().stream()
                .map(producto -> List.of(String.valueOf(producto.getId()), producto.obtenerNombre(),
                        producto.getPrecio().toPlainString(), String.valueOf(producto.getStock())))
                .toList();
        Map<String, String> resumen = new LinkedHashMap<>();
        resumen.put("Categoría", nodoRaiz.obtenerNombre());
        resumen.put("Total de productos", String.valueOf(nodoRaiz.contarProductos()));
        resumen.put("Unidades en stock", String.valueOf(nodoRaiz.obtenerStockTotal()));
        resumen.put("Suma de precios (S/)", nodoRaiz.obtenerPrecioTotal().toPlainString());
        return new ReporteTabla("Catálogo de productos — " + nodoRaiz.obtenerNombre(), COLUMNAS, filas, resumen);
    }

    @Override
    protected String nombreBase() {
        return "catalogo-nexomarket";
    }
}
