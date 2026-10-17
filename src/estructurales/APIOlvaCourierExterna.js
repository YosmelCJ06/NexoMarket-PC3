// Simula una API externa. No llama al servicio real de Olva Courier.
export class APIOlvaCourierExterna {
  crearTracking(codigoCliente, idCompra) { return `OLVA-TRACK-${codigoCliente}-${idCompra}`; }
}
