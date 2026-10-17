import { PagoFactory } from '../creacionales/PagoFactory.js';
import { EnvioPedido } from '../comportamiento/EnvioPedido.js';
import { HistorialSeguimientoModulo } from '../comportamiento/HistorialSeguimientoModulo.js';
import { ClienteNotificacionModulo } from '../comportamiento/ClienteNotificacionModulo.js';
let siguientePedidoId = 10001;
export class CheckoutFacade {
  realizarCompra(clienteId, carritoMonto, tipoPago) {
    console.log('=== INICIANDO CHECKOUT (DEMO) ===');
    if (!Number.isInteger(clienteId) || clienteId <= 0) throw new Error('Cliente inválido');
    if (!Number.isFinite(carritoMonto) || carritoMonto <= 0) throw new Error('Monto inválido');
    console.log('[Inventario SIMULADO] Stock verificado.');
    const metodoPago = PagoFactory.crearMetodoPago(tipoPago);
    if (!metodoPago.procesarPago(carritoMonto)) throw new Error('Pago rechazado');
    const pedidoId = siguientePedidoId++;
    console.log(`[Pedido SIMULADO] Pedido #${pedidoId} creado.`);
    const envio = new EnvioPedido(pedidoId);
    envio.agregarObservador(new HistorialSeguimientoModulo());
    envio.agregarObservador(new ClienteNotificacionModulo());
    envio.cambiarEstado('PREPARANDO');
    console.log('=== CHECKOUT DEMO FINALIZADO ===');
    return { pedidoId, clienteId, monto: carritoMonto, tipoPago, estado: envio.estadoActual, simulacion: true };
  }
}
