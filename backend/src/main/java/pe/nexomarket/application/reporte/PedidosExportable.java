package pe.nexomarket.application.reporte;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import pe.nexomarket.application.pedido.consulta.DetallePedido;

/**
 * Patrón BRIDGE — segunda "RefinedAbstraction": reporte de pedidos.
 * Demuestra que se agrega un reporte nuevo sin crear clases por cada formato.
 */
public class PedidosExportable extends Exportable {
    private static final List<String> COLUMNAS = List.of("Código", "Cliente", "Monto (S/)", "Estado");
    private final List<DetallePedido> pedidos;

    public PedidosExportable(List<DetallePedido> pedidos, FormatoExportacion formato) {
        super(formato);
        this.pedidos = List.copyOf(pedidos);
    }

    @Override
    protected ReporteTabla construirReporte() {
        List<List<String>> filas = pedidos.stream()
                .map(pedido -> List.of(pedido.codigo(), String.valueOf(pedido.clienteId()),
                        pedido.montoTotal().toPlainString(), pedido.estado()))
                .toList();
        BigDecimal total = pedidos.stream().map(DetallePedido::montoTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, String> resumen = new LinkedHashMap<>();
        resumen.put("Cantidad de pedidos", String.valueOf(pedidos.size()));
        resumen.put("Monto total (S/)", total.toPlainString());
        return new ReporteTabla("Reporte de pedidos", COLUMNAS, filas, resumen);
    }

    @Override
    protected String nombreBase() {
        return "pedidos-nexomarket";
    }
}
