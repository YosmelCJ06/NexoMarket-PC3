// Singleton didáctico: simula el gestor de base de datos, NO usa PostgreSQL real.
export class ConexionBaseDatos {
  static #instanciaUnica;
  constructor(token) {
    if (token !== ConexionBaseDatos) throw new Error('Utilice obtenerInstancia()');
    this.estadoConexion = 'Simulación de conexión a PostgreSQL (NexoMarket)';
  }
  static obtenerInstancia() {
    if (!this.#instanciaUnica) this.#instanciaUnica = new ConexionBaseDatos(ConexionBaseDatos);
    return this.#instanciaUnica;
  }
  consultar(query) {
    console.log(`[BD SIMULADA] Ejecutando: ${query}`);
    return { simulacion: true, consulta: query };
  }
}
