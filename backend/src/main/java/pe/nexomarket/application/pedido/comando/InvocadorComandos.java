package pe.nexomarket.application.pedido.comando;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Patrón COMMAND — participante "Invoker".
 * Ejecuta comandos sin conocer su lógica, guarda los ejecutados para poder
 * deshacerlos y deja un registro de auditoría de todo lo que pasó.
 */
public class InvocadorComandos {
    private final Deque<Comando> ejecutados = new ArrayDeque<>();
    private final List<RegistroComando> registro = new ArrayList<>();

    public synchronized void ejecutarComando(Comando comando) {
        Objects.requireNonNull(comando);
        comando.ejecutar();
        ejecutados.push(comando);
        registro.add(new RegistroComando(comando.descripcion(), RegistroComando.Tipo.EJECUTADO, Instant.now()));
    }

    public synchronized Comando deshacerUltimo() {
        return deshacerUltimo(comando -> true);
    }

    /** Deshace el comando más reciente que cumpla el filtro (por ejemplo, de un pedido). */
    public synchronized Comando deshacerUltimo(Predicate<Comando> filtro) {
        Iterator<Comando> iterador = ejecutados.iterator();
        while (iterador.hasNext()) {
            Comando comando = iterador.next();
            if (filtro.test(comando)) {
                comando.deshacer();
                iterador.remove();
                registro.add(new RegistroComando(comando.descripcion(), RegistroComando.Tipo.DESHECHO, Instant.now()));
                return comando;
            }
        }
        throw new IllegalStateException("No hay comandos ejecutados para deshacer.");
    }

    public synchronized int getTotalComandosEjecutados() {
        return ejecutados.size();
    }

    public synchronized List<RegistroComando> getRegistro() {
        return List.copyOf(registro);
    }

    /** Entrada del log de auditoría del Invoker. */
    public record RegistroComando(String descripcion, Tipo tipo, Instant fecha) {
        public enum Tipo { EJECUTADO, DESHECHO }
    }
}
