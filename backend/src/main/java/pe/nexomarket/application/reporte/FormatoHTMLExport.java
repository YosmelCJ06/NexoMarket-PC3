package pe.nexomarket.application.reporte;

import java.util.Map;

/** Patrón BRIDGE — "ConcreteImplementor": escribe el reporte como tabla HTML. */
public class FormatoHTMLExport implements FormatoExportacion {

    @Override
    public String exportar(ReporteTabla reporte) {
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html lang=\"es\"><head><meta charset=\"utf-8\"><title>")
                .append(escapar(reporte.titulo())).append("</title></head><body>");
        html.append("<h1>").append(escapar(reporte.titulo())).append("</h1><table border=\"1\"><thead><tr>");
        reporte.columnas().forEach(columna -> html.append("<th>").append(escapar(columna)).append("</th>"));
        html.append("</tr></thead><tbody>");
        for (var fila : reporte.filas()) {
            html.append("<tr>");
            fila.forEach(celda -> html.append("<td>").append(escapar(celda)).append("</td>"));
            html.append("</tr>");
        }
        html.append("</tbody></table><ul>");
        for (Map.Entry<String, String> total : reporte.resumen().entrySet()) {
            html.append("<li><strong>").append(escapar(total.getKey())).append(":</strong> ")
                    .append(escapar(total.getValue())).append("</li>");
        }
        return html.append("</ul></body></html>").toString();
    }

    @Override
    public String tipoContenido() {
        return "text/html; charset=UTF-8";
    }

    @Override
    public String extension() {
        return "html";
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
