package pe.nexomarket.application.reporte;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Patrón BRIDGE — "ConcreteImplementor": escribe el reporte como CSV,
 * un archivo que Excel abre directamente.
 */
public class FormatoCSVExport implements FormatoExportacion {

    @Override
    public String exportar(ReporteTabla reporte) {
        StringBuilder csv = new StringBuilder();
        csv.append(linea(reporte.columnas()));
        reporte.filas().forEach(fila -> csv.append(linea(fila)));
        csv.append("\r\n");
        for (Map.Entry<String, String> total : reporte.resumen().entrySet()) {
            csv.append(linea(List.of(total.getKey(), total.getValue())));
        }
        return csv.toString();
    }

    @Override
    public String tipoContenido() {
        return "text/csv; charset=UTF-8";
    }

    @Override
    public String extension() {
        return "csv";
    }

    private static String linea(List<String> celdas) {
        return celdas.stream().map(FormatoCSVExport::celda).collect(Collectors.joining(",")) + "\r\n";
    }

    private static String celda(String valor) {
        boolean necesitaComillas = valor.contains(",") || valor.contains("\"") || valor.contains("\n");
        String escapado = valor.replace("\"", "\"\"");
        return necesitaComillas ? "\"" + escapado + "\"" : escapado;
    }
}
