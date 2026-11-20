package pe.nexomarket.domain.seguridad;

import java.util.Locale;

/** Roles del sistema. Reemplaza los textos "CLIENTE"/"ADMINISTRADOR" repetidos en el código. */
public enum Rol {
    INVITADO,
    CLIENTE,
    ADMINISTRADOR;

    public static Rol desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) return INVITADO;
        try {
            return valueOf(texto.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return INVITADO;
        }
    }
}
