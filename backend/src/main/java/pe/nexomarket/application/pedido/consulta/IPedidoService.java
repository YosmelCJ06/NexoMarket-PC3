package pe.nexomarket.application.pedido.consulta;

/**
 * Patrón PROXY — participante "Subject".
 * Interfaz común del servicio real y del proxy: el cliente no sabe cuál usa.
 */
public interface IPedidoService {
    DetallePedido obtenerDetallePedido(int pedidoId);
}
