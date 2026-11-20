import { ConexionBaseDatos } from './creacionales/ConexionBaseDatos.js';
import { OlvaCourierAdapter } from './estructurales/OlvaCourierAdapter.js';
import { ProductoBase } from './estructurales/ProductoBase.js';
import { GarantiaDecorator } from './estructurales/GarantiaDecorator.js';
import { RegaloDecorator } from './estructurales/RegaloDecorator.js';
import { CheckoutFacade } from './estructurales/CheckoutFacade.js';

console.log('NEXOMARKET — DEMO PC2 (operaciones simuladas)');
console.log('\n--- SINGLETON ---');
const conexion1 = ConexionBaseDatos.obtenerInstancia();
const conexion2 = ConexionBaseDatos.obtenerInstancia();
conexion1.consultar('SELECT * FROM PRODUCTO');
console.log('¿Misma instancia?', conexion1 === conexion2);

console.log('\n--- ADAPTER ---');
console.log('Guía:', new OlvaCourierAdapter().generarGuiaEnvio(10023));

console.log('\n--- DECORATOR ---');
let producto = new ProductoBase();
for (const etiqueta of ['Base', 'Garantía', 'Garantía + regalo']) {
  if (etiqueta === 'Garantía') producto = new GarantiaDecorator(producto);
  if (etiqueta === 'Garantía + regalo') producto = new RegaloDecorator(producto);
  console.log(`${etiqueta}: ${producto.obtenerDescripcion()} -> $${producto.obtenerPrecio().toFixed(2)}`);
}

console.log('\n--- FACTORY + STRATEGY + FACADE + OBSERVER ---');
const compra = new CheckoutFacade().realizarCompra(1, 250, 'YAPE');
console.log('Resultado:', compra);
