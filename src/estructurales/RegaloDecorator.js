export class RegaloDecorator {
  constructor(item) { this.item = item; }
  obtenerDescripcion() { return this.item.obtenerDescripcion() + ' + Envoltorio de Regalo'; }
  obtenerPrecio() { return this.item.obtenerPrecio() + 15; }
}
