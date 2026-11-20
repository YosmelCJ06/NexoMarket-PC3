import { PagoTarjeta } from '../comportamiento/PagoTarjeta.js';
import { PagoYape } from '../comportamiento/PagoYape.js';
// Fábrica simple; no pretende ser la versión GoF estricta de Factory Method.
export class PagoFactory {
  static crearMetodoPago(tipo) {
    switch (tipo?.toUpperCase()) {
      case 'TARJETA': return new PagoTarjeta();
      case 'YAPE': return new PagoYape();
      default: throw new Error(`Método de pago no soportado: ${tipo}`);
    }
  }
}
