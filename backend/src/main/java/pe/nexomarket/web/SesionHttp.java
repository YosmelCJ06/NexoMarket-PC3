package pe.nexomarket.web;

import jakarta.servlet.http.HttpSession;
import pe.nexomarket.domain.seguridad.Rol;
import pe.nexomarket.domain.seguridad.UsuarioSesion;

/**
 * Nombres de los atributos de la sesión HTTP en un solo lugar y conversión a
 * {@link UsuarioSesion}. Evita repetir textos como "nexomarket.role" en cada
 * controlador (antipatrón Magic Strings / Copy-Paste Programming).
 */
public final class SesionHttp {
    public static final String USUARIO_ID = "nexomarket.userId";
    public static final String CLIENTE_ID = "nexomarket.clientId";
    public static final String EMAIL = "nexomarket.email";
    public static final String NOMBRE = "nexomarket.name";
    public static final String ROL = "nexomarket.role";

    private SesionHttp() {
    }

    public static UsuarioSesion usuario(HttpSession sesion) {
        if (sesion == null) return UsuarioSesion.invitado();
        Integer usuarioId = (Integer) sesion.getAttribute(USUARIO_ID);
        if (usuarioId == null) return UsuarioSesion.invitado();
        Integer clienteId = (Integer) sesion.getAttribute(CLIENTE_ID);
        return new UsuarioSesion(usuarioId, clienteId, Rol.desdeTexto((String) sesion.getAttribute(ROL)));
    }
}
