package pe.nexomarket.application.reporte;

import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import pe.nexomarket.application.pedido.consulta.DetallePedido;
import pe.nexomarket.domain.catalogo.CatalogoRepository;
import pe.nexomarket.domain.pedido.PedidoRepository;
import pe.nexomarket.domain.seguridad.AccesoDenegadoException;
import pe.nexomarket.domain.seguridad.UsuarioSesion;

/**
 * Arma los reportes con el patrón BRIDGE: el reporte (abstracción) se combina
 * con el formato pedido (implementación) en tiempo de ejecución.
 */
@Service
public class ReporteService {
    private static final Map<String, Supplier<FormatoExportacion>> FORMATOS = Map.of(
            "html", FormatoHTMLExport::new,
            "csv", FormatoCSVExport::new);

    private final CatalogoRepository catalogo;
    private final PedidoRepository pedidos;

    public ReporteService(CatalogoRepository catalogo, PedidoRepository pedidos) {
        this.catalogo = catalogo;
        this.pedidos = pedidos;
    }

    public Exportable reporteCatalogo(String formato, UsuarioSesion usuario) {
        exigirAdministrador(usuario);
        return new CatalogoExportable(catalogo.cargarCatalogo(), formato(formato));
    }

    public Exportable reportePedidos(String formato, UsuarioSesion usuario) {
        exigirAdministrador(usuario);
        return new PedidosExportable(pedidos.listar().stream().map(DetallePedido::desde).toList(), formato(formato));
    }

    private static FormatoExportacion formato(String nombre) {
        Supplier<FormatoExportacion> fabrica = nombre == null ? null : FORMATOS.get(nombre.trim().toLowerCase(Locale.ROOT));
        if (fabrica == null) throw new IllegalArgumentException("Formato no soportado: " + nombre + ". Usa html o csv.");
        return fabrica.get();
    }

    private static void exigirAdministrador(UsuarioSesion usuario) {
        if (usuario == null || !usuario.esAdministrador()) {
            throw new AccesoDenegadoException("Se requiere una sesión de administrador.");
        }
    }
}
