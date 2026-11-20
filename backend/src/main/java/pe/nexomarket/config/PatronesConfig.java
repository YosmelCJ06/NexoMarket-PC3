package pe.nexomarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.nexomarket.application.pedido.comando.InvocadorComandos;
import pe.nexomarket.domain.pedido.memento.HistorialPedidoCaretaker;
import pe.nexomarket.domain.pedido.observer.AuditoriaPedidoObservador;

/**
 * Registra como beans de Spring los objetos de los patrones que deben vivir
 * mientras la aplicación esté encendida: el Invoker con su historial de
 * comandos, el Caretaker de mementos y el observador de auditoría.
 */
@Configuration
public class PatronesConfig {

    @Bean
    public InvocadorComandos invocadorComandos() {
        return new InvocadorComandos();
    }

    @Bean
    public HistorialPedidoCaretaker historialPedidoCaretaker() {
        return new HistorialPedidoCaretaker();
    }

    @Bean
    public AuditoriaPedidoObservador auditoriaPedidoObservador() {
        return new AuditoriaPedidoObservador();
    }
}
