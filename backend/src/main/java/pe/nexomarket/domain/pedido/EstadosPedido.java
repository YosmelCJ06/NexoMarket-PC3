package pe.nexomarket.domain.pedido;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Traduce el código guardado en la base de datos (columna pedido.estado)
 * al objeto State correspondiente. Un mapa reemplaza al switch de textos.
 */
public final class EstadosPedido {
    private static final Map<String, Supplier<EstadoPedido>> POR_CODIGO = new LinkedHashMap<>();

    static {
        POR_CODIGO.put(EstadoPendiente.CODIGO, EstadoPendiente::new);
        POR_CODIGO.put(EstadoPagado.CODIGO, EstadoPagado::new);
        POR_CODIGO.put(EstadoEnPreparacion.CODIGO, EstadoEnPreparacion::new);
        POR_CODIGO.put(EstadoEnviado.CODIGO, EstadoEnviado::new);
        POR_CODIGO.put(EstadoEntregado.CODIGO, EstadoEntregado::new);
        POR_CODIGO.put(EstadoCancelado.CODIGO, EstadoCancelado::new);
    }

    private EstadosPedido() {
    }

    public static EstadoPedido desdeCodigo(String codigo) {
        if (codigo == null) throw new IllegalArgumentException("El estado del pedido es obligatorio.");
        Supplier<EstadoPedido> fabrica = POR_CODIGO.get(codigo.trim().toUpperCase(Locale.ROOT));
        if (fabrica == null) throw new IllegalArgumentException("Estado de pedido desconocido: " + codigo);
        return fabrica.get();
    }

    public static boolean esValido(String codigo) {
        return codigo != null && POR_CODIGO.containsKey(codigo.trim().toUpperCase(Locale.ROOT));
    }

    public static List<String> codigos() {
        return List.copyOf(POR_CODIGO.keySet());
    }
}
