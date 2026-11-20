package pe.nexomarket.controller;

import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.nexomarket.application.reporte.Exportable;
import pe.nexomarket.application.reporte.ReporteService;
import pe.nexomarket.web.SesionHttp;

/** Descarga de reportes (Bridge + Composite): /api/reports/catalog?formato=csv */
@RestController
@RequestMapping("/api/reports")
public class ReporteController {
    private final ReporteService servicio;

    public ReporteController(ReporteService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/catalog")
    public ResponseEntity<String> catalogo(@RequestParam(defaultValue = "html") String formato, HttpSession sesion) {
        return responder(servicio.reporteCatalogo(formato, SesionHttp.usuario(sesion)));
    }

    @GetMapping("/orders")
    public ResponseEntity<String> pedidos(@RequestParam(defaultValue = "html") String formato, HttpSession sesion) {
        return responder(servicio.reportePedidos(formato, SesionHttp.usuario(sesion)));
    }

    private static ResponseEntity<String> responder(Exportable reporte) {
        boolean esHtml = reporte.tipoContenido().startsWith("text/html");
        ContentDisposition disposicion = (esHtml ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(reporte.nombreArchivo(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(reporte.tipoContenido()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .body(reporte.generarReporte());
    }
}
