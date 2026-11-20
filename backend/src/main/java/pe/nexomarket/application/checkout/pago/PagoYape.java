package pe.nexomarket.application.checkout.pago;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** ConcreteStrategy: pago con Yape (simulado). */
public class PagoYape implements MetodoPago {
    private static final Logger log = LoggerFactory.getLogger(PagoYape.class);

    @Override
    public boolean procesarPago(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) return false;
        log.info("[PAGO SIMULADO] S/ {} mediante Yape.", monto);
        return true;
    }

    @Override
    public String nombre() {
        return "YAPE";
    }
}
