import test from 'node:test';
import assert from 'node:assert/strict';
import { ConexionBaseDatos } from '../src/creacionales/ConexionBaseDatos.js';
import { PagoFactory } from '../src/creacionales/PagoFactory.js';
import { OlvaCourierAdapter } from '../src/estructurales/OlvaCourierAdapter.js';
import { ProductoBase } from '../src/estructurales/ProductoBase.js';
import { GarantiaDecorator } from '../src/estructurales/GarantiaDecorator.js';
import { RegaloDecorator } from '../src/estructurales/RegaloDecorator.js';
import { EnvioPedido } from '../src/comportamiento/EnvioPedido.js';
import { CheckoutFacade } from '../src/estructurales/CheckoutFacade.js';

test('Singleton reutiliza la misma instancia', () => {
  assert.equal(ConexionBaseDatos.obtenerInstancia(), ConexionBaseDatos.obtenerInstancia());
  assert.throws(() => new ConexionBaseDatos());
});
test('Factory selecciona métodos de pago', () => {
  assert.equal(PagoFactory.crearMetodoPago('YAPE').constructor.name, 'PagoYape');
  assert.equal(PagoFactory.crearMetodoPago('TARJETA').constructor.name, 'PagoTarjeta');
  assert.throws(() => PagoFactory.crearMetodoPago('OTRO'));
});
test('Adapter genera guía traduciendo parámetros', () => {
  assert.equal(new OlvaCourierAdapter().generarGuiaEnvio(12), 'OLVA-TRACK-NEXO-99-12');
});
test('Decorator permite componer extras', () => {
  const item = new RegaloDecorator(new GarantiaDecorator(new ProductoBase()));
  assert.equal(item.obtenerPrecio(), 1464.99);
  assert.match(item.obtenerDescripcion(), /Garantía/);
});
test('Observer notifica cambios', () => {
  const cambios=[];
  const envio = new EnvioPedido(8);
  envio.agregarObservador({ actualizarEstado(id, estado) { cambios.push([id,estado]); } });
  envio.cambiarEstado('PREPARANDO');
  assert.deepEqual(cambios, [[8,'PREPARANDO']]);
});
test('Facade completa un checkout simulado', () => {
  const compra = new CheckoutFacade().realizarCompra(1, 250, 'YAPE');
  assert.equal(compra.estado, 'PREPARANDO');
  assert.equal(compra.simulacion, true);
});
