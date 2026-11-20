package pe.nexomarket.application.reporte;

import java.util.Objects;

/**
 * Patrón BRIDGE — participante "Abstraction".
 * Sabe QUÉ datos lleva un reporte y delega en su {@link FormatoExportacion}
 * (el puente) CÓMO se escriben. Ambas jerarquías crecen por separado:
 * un reporte nuevo funciona con todos los formatos y viceversa.
 */
public abstract class Exportable {
    protected final FormatoExportacion formato;

    protected Exportable(FormatoExportacion formato) {
        this.formato = Objects.requireNonNull(formato);
    }

    public final String generarReporte() {
        return formato.exportar(construirReporte());
    }

    public String nombreArchivo() {
        return nombreBase() + "." + formato.extension();
    }

    public String tipoContenido() {
        return formato.tipoContenido();
    }

    protected abstract ReporteTabla construirReporte();

    protected abstract String nombreBase();
}
