package pe.nexomarket.application.pedido.comando;

/**
 * Patrón COMMAND — participante "Command".
 * Encapsula una solicitud como objeto para poder ejecutarla, registrarla
 * (auditoría) y revertirla (deshacer).
 */
public interface Comando {
    void ejecutar();

    void deshacer();

    String descripcion();
}
