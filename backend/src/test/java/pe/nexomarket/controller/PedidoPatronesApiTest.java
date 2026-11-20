package pe.nexomarket.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pe.nexomarket.application.pedido.PedidoAccionService;
import pe.nexomarket.application.reporte.ReporteService;
import pe.nexomarket.config.PatronesConfig;
import pe.nexomarket.domain.catalogo.CatalogoRepository;
import pe.nexomarket.domain.catalogo.CategoriaComposite;
import pe.nexomarket.domain.catalogo.ProductoHoja;
import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.infrastructure.memoria.PedidoRepositoryEnMemoria;
import pe.nexomarket.web.SesionHttp;

/** Prueba los endpoints de los patrones sin PostgreSQL, con repositorios en memoria. */
@WebMvcTest(controllers = {PedidoPatronesController.class, ReporteController.class})
@Import({PedidoAccionService.class, ReporteService.class, PatronesConfig.class,
        PedidoPatronesApiTest.RepositoriosEnMemoria.class})
class PedidoPatronesApiTest {
    private static final Map<String, Object> ADMIN = Map.of(
            SesionHttp.USUARIO_ID, 1, SesionHttp.ROL, "ADMINISTRADOR");
    private static final Map<String, Object> CLIENTE_102 = Map.of(
            SesionHttp.USUARIO_ID, 10, SesionHttp.CLIENTE_ID, 102, SesionHttp.ROL, "CLIENTE");
    private static final Map<String, Object> OTRO_CLIENTE = Map.of(
            SesionHttp.USUARIO_ID, 11, SesionHttp.CLIENTE_ID, 999, SesionHttp.ROL, "CLIENTE");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PedidoRepositoryEnMemoria repositorio;

    @TestConfiguration
    static class RepositoriosEnMemoria {
        @Bean
        PedidoRepositoryEnMemoria pedidoRepository() {
            PedidoRepositoryEnMemoria repositorio = new PedidoRepositoryEnMemoria();
            repositorio.registrarCorreo(102, "cliente@nexomarket.pe");
            return repositorio;
        }

        @Bean
        CatalogoRepository catalogoRepository() {
            return () -> new CategoriaComposite("Catálogo NexoMarket")
                    .agregar(new CategoriaComposite("Computadoras")
                            .agregar(new ProductoHoja(1, "MacBook Pro 14", new BigDecimal("7499.90"), 2)));
        }
    }

    @BeforeEach
    void pedidosDePrueba() {
        for (int id = 1; id <= 6; id++) {
            repositorio.guardar(new Pedido(id, 102, new BigDecimal("250.00")));
        }
    }

    @Test
    void proxyRechazaInvitadoYClienteAjenoPeroPermiteAlDuenio() throws Exception {
        mvc.perform(get("/api/orders/1")).andExpect(status().isForbidden());
        mvc.perform(get("/api/orders/1").sessionAttrs(OTRO_CLIENTE)).andExpect(status().isForbidden());
        mvc.perform(get("/api/orders/1").sessionAttrs(CLIENTE_102))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("PED-00001"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
        mvc.perform(get("/api/orders/77").sessionAttrs(ADMIN)).andExpect(status().isNotFound());
    }

    @Test
    void ejecutaAccionesConStateYRechazaTransicionesInvalidas() throws Exception {
        accion(2, "pagar").andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("PAGADO"));
        accion(2, "preparar").andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("PREPARANDO"));
        accion(2, "entregar").andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("PREPARANDO")));
        accion(2, "borrar").andExpect(status().isBadRequest());
        assert repositorio.buscarPorId(2).orElseThrow().getNombreEstado().equals("PREPARANDO");
    }

    @Test
    void soloElAdministradorEjecutaAcciones() throws Exception {
        mvc.perform(post("/api/orders/3/acciones").sessionAttrs(CLIENTE_102)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"accion\":\"pagar\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deshaceConMementoYMuestraElHistorial() throws Exception {
        accion(4, "pagar").andExpect(status().isOk());
        accion(4, "preparar").andExpect(status().isOk());

        mvc.perform(post("/api/orders/4/deshacer").sessionAttrs(ADMIN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("PAGADO"));

        mvc.perform(get("/api/orders/4/historial").sessionAttrs(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventos", hasSize(3)))
                .andExpect(jsonPath("$.eventos[2].detalle").value(containsString("Memento")))
                .andExpect(jsonPath("$.comandos", hasSize(3)))
                .andExpect(jsonPath("$.comandos[2].tipo").value("DESHECHO"))
                .andExpect(jsonPath("$.puedeDeshacer").value(true));

        mvc.perform(post("/api/orders/4/deshacer").sessionAttrs(ADMIN)).andExpect(status().isOk());
        mvc.perform(post("/api/orders/4/deshacer").sessionAttrs(ADMIN)).andExpect(status().isConflict());
    }

    @Test
    void exportaReportesEnDistintosFormatosConBridge() throws Exception {
        mvc.perform(get("/api/reports/catalog").param("formato", "csv").sessionAttrs(ADMIN))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("catalogo-nexomarket.csv")))
                .andExpect(content().string(containsString("1,MacBook Pro 14,7499.90,2")));
        mvc.perform(get("/api/reports/orders").param("formato", "html").sessionAttrs(ADMIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<td>PED-00005</td>")));
        mvc.perform(get("/api/reports/catalog").param("formato", "pdf").sessionAttrs(ADMIN))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/reports/catalog").sessionAttrs(CLIENTE_102)).andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.ResultActions accion(int id, String accion) throws Exception {
        return mvc.perform(post("/api/orders/" + id + "/acciones").sessionAttrs(ADMIN)
                .contentType(MediaType.APPLICATION_JSON).content("{\"accion\":\"" + accion + "\"}"));
    }
}
