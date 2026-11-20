package pe.nexomarket.application.reporte;

/**
 * Patrón BRIDGE — participante "Implementor".
 * Define cómo se escribe un reporte en un formato concreto (HTML, CSV...).
 */
public interface FormatoExportacion {
    String exportar(ReporteTabla reporte);

    String tipoContenido();

    String extension();
}
