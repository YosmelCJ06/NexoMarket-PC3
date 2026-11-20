package pe.nexomarket.application.checkout;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.nexomarket.application.pedido.PedidoAccionService;
import pe.nexomarket.application.pedido.consulta.DetallePedido;
import pe.nexomarket.domain.pedido.LineaPedido;
import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.seguridad.AccesoDenegadoException;
import pe.nexomarket.domain.seguridad.UsuarioSesion;

/**
 * Patrón FACADE refactorizado (antes era un God Object en la PC2).
 * Solo coordina la compra; cada paso lo hace un servicio especializado que
 * recibe por inyección de dependencias. La transacción revierte el stock y el
 * pedido si el pago falla.
 */
@Service
public class CheckoutFacade {
    private final ServicioInventario inventario;
    private final ServicioPago pago;
    private final ServicioPedido pedidos;
    private final PedidoAccionService acciones;

    public CheckoutFacade(ServicioInventario inventario, ServicioPago pago, ServicioPedido pedidos,
            PedidoAccionService acciones) {
        this.inventario = inventario;
        this.pago = pago;
        this.pedidos = pedidos;
        this.acciones = acciones;
    }

    @Transactional
    public DetallePedido comprar(UsuarioSesion cliente, List<ItemCarrito> items, String metodoPago) {
        if (cliente == null || !cliente.esCliente() || cliente.clienteId() == null) {
            throw new AccesoDenegadoException("Inicia sesión como cliente para comprar.");
        }
        List<LineaPedido> lineas = inventario.reservar(items);                     // 1. Inventario
        BigDecimal total = lineas.stream().map(LineaPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        Pedido pedido = pedidos.crear(cliente.clienteId(), lineas);               // 2. Pedido (PENDIENTE)
        pago.cobrar(metodoPago, total);                                           // 3. Pago (Strategy)
        return acciones.confirmarPago(pedido.getId());                            // 4. Command + State + Observer
    }
}
