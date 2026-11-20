package pe.nexomarket.application.pedido.consulta;

import java.math.BigDecimal;
import pe.nexomarket.domain.pedido.Pedido;

/** Datos de un pedido que se devuelven al consultar. */
public record DetallePedido(int id, String codigo, int clienteId, BigDecimal montoTotal, String estado) {

    public static DetallePedido desde(Pedido pedido) {
        return new DetallePedido(pedido.getId(), pedido.getCodigo(), pedido.getClienteId(),
                pedido.getMontoTotal(), pedido.getNombreEstado());
    }
}
