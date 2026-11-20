package pe.nexomarket.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.nexomarket.application.pedido.PedidoAccionService;
import pe.nexomarket.application.pedido.PedidoAccionService.HistorialPedido;
import pe.nexomarket.application.pedido.consulta.DetallePedido;
import pe.nexomarket.web.SesionHttp;

/**
 * Endpoints de un pedido que usan los patrones de la PC3. El controlador solo
 * traduce HTTP: la lógica está en {@link PedidoAccionService} y los errores
 * los convierte {@link ApiExceptionHandler}.
 */
@RestController
@RequestMapping("/api/orders/{id}")
public class PedidoPatronesController {
    private final PedidoAccionService servicio;

    public PedidoPatronesController(PedidoAccionService servicio) {
        this.servicio = servicio;
    }

    /** Proxy: GET /api/orders/{id} */
    @GetMapping
    public DetallePedido detalle(@PathVariable int id, HttpSession sesion) {
        return servicio.consultar(id, SesionHttp.usuario(sesion));
    }

    /** Command + State + Observer: POST /api/orders/{id}/acciones {"accion": "pagar"} */
    @PostMapping("/acciones")
    public DetallePedido ejecutar(@PathVariable int id, @RequestBody AccionRequest peticion, HttpSession sesion) {
        return servicio.ejecutarAccion(id, peticion == null ? null : peticion.accion(), SesionHttp.usuario(sesion));
    }

    /** Memento: POST /api/orders/{id}/deshacer */
    @PostMapping("/deshacer")
    public DetallePedido deshacer(@PathVariable int id, HttpSession sesion) {
        return servicio.deshacer(id, SesionHttp.usuario(sesion));
    }

    /** Auditoría (Observer) y registro de comandos: GET /api/orders/{id}/historial */
    @GetMapping("/historial")
    public HistorialPedido historial(@PathVariable int id, HttpSession sesion) {
        return servicio.historial(id, SesionHttp.usuario(sesion));
    }

    public record AccionRequest(String accion) {
    }
}
