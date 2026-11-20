package pe.nexomarket.domain.seguridad;

import java.util.Objects;

/** Usuario que hace la solicitud. clienteId es null para administradores e invitados. */
public record UsuarioSesion(Integer usuarioId, Integer clienteId, Rol rol) {

    public UsuarioSesion {
        Objects.requireNonNull(rol, "El rol es obligatorio.");
    }

    public static UsuarioSesion invitado() {
        return new UsuarioSesion(null, null, Rol.INVITADO);
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    public boolean esCliente() {
        return rol == Rol.CLIENTE;
    }
}
