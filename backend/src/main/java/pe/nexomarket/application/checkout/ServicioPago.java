package pe.nexomarket.application.checkout;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import pe.nexomarket.application.checkout.pago.FabricaMetodoPago;
import pe.nexomarket.application.checkout.pago.MetodoPago;

/** Responsabilidad única: cobrar con el método de pago elegido (Strategy + fábrica). */
@Service
public class ServicioPago {

    public void cobrar(String tipoPago, BigDecimal monto) {
        MetodoPago metodo = FabricaMetodoPago.crear(tipoPago);
        if (!metodo.procesarPago(monto)) {
            throw new IllegalStateException("El pago con " + metodo.nombre() + " fue rechazado.");
        }
    }
}
