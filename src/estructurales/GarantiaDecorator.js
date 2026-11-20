export class GarantiaDecorator {
  constructor(item) { this.item = item; }
  obtenerDescripcion() { return this.item.obtenerDescripcion() + ' + Garantía 2 años'; }
  obtenerPrecio() { return this.item.obtenerPrecio() + 150; }
}
