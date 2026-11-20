package pe.nexomarket.application.checkout.pago;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/** Fábrica de métodos de pago (heredada de la PC2): crea la estrategia según el tipo pedido. */
public final class FabricaMetodoPago {
    private static final Map<String, Supplier<MetodoPago>> METODOS = Map.of(
            "TARJETA", PagoTarjeta::new,
            "YAPE", PagoYape::new);

    private FabricaMetodoPago() {
    }

    public static MetodoPago crear(String tipo) {
        Supplier<MetodoPago> fabrica = tipo == null ? null : METODOS.get(tipo.trim().toUpperCase(Locale.ROOT));
        if (fabrica == null) throw new IllegalArgumentException("Método de pago no soportado: " + tipo);
        return fabrica.get();
    }
}
