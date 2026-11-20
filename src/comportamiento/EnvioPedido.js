export class EnvioPedido {
  constructor(pedidoId) {
    this.pedidoId = pedidoId;
    this.observadores = [];
    this.estadoActual = 'CONFIRMADO';
  }
  agregarObservador(observador) {
    if (typeof observador?.actualizarEstado !== 'function') throw new Error('Observador inválido');
    this.observadores.push(observador);
  }
  cambiarEstado(nuevoEstado) {
    this.estadoActual = nuevoEstado;
    this.observadores.forEach(obs => obs.actualizarEstado(this.pedidoId, nuevoEstado));
  }
}
