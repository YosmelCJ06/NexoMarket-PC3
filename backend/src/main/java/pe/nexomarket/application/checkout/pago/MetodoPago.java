package pe.nexomarket.application.checkout.pago;

import java.math.BigDecimal;

/**
 * Patrón STRATEGY (heredado de la PC2): cada método de pago encapsula su
 * propia forma de cobrar. Los pagos son simulados; no se conecta a una pasarela.
 */
public interface MetodoPago {
    boolean procesarPago(BigDecimal monto);

    String nombre();
}
