import { APIOlvaCourierExterna } from './APIOlvaCourierExterna.js';
export class OlvaCourierAdapter {
  constructor(apiExterna = new APIOlvaCourierExterna()) { this.apiExterna = apiExterna; }
  generarGuiaEnvio(pedidoId) { return this.apiExterna.crearTracking('NEXO-99', pedidoId); }
}
