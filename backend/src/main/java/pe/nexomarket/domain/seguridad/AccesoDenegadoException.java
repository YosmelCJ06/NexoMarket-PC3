package pe.nexomarket.domain.seguridad;

/** El usuario no tiene permiso para la operación solicitada. */
public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
