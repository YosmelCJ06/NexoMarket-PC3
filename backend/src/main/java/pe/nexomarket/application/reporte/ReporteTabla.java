package pe.nexomarket.application.reporte;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contenido neutro de un reporte (título, columnas, filas y totales).
 * La Abstraction lo arma y el Implementor decide cómo se ve.
 */
public record ReporteTabla(String titulo, List<String> columnas, List<List<String>> filas, Map<String, String> resumen) {

    public ReporteTabla {
        columnas = List.copyOf(columnas);
        filas = filas.stream().map(List::copyOf).toList();
        resumen = new LinkedHashMap<>(resumen);
        for (List<String> fila : filas) {
            if (fila.size() != columnas.size()) {
                throw new IllegalArgumentException("Cada fila debe tener " + columnas.size() + " columnas.");
            }
        }
    }
}
