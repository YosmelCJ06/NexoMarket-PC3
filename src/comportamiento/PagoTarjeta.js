export class PagoTarjeta {
  procesarPago(monto) {
    if (!Number.isFinite(monto) || monto <= 0) return false;
    console.log(`[Pago SIMULADO] S/${monto.toFixed(2)} con Tarjeta.`);
    return true;
  }
}
