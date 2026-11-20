package pe.nexomarket.domain.pedido.memento;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import pe.nexomarket.domain.pedido.PedidoMemento;

/**
 * Patrón MEMENTO — participante "Caretaker".
 * Guarda una pila de mementos por pedido. No puede leer ni modificar su
 * contenido (los métodos de lectura de PedidoMemento son de paquete).
 */
public class HistorialPedidoCaretaker {
    private final Map<Integer, Deque<PedidoMemento>> historialPorPedido = new HashMap<>();

    public synchronized void guardar(int pedidoId, PedidoMemento memento) {
        historialPorPedido.computeIfAbsent(pedidoId, id -> new ArrayDeque<>()).push(memento);
    }

    public synchronized PedidoMemento deshacer(int pedidoId) {
        Deque<PedidoMemento> pila = historialPorPedido.get(pedidoId);
        if (pila == null || pila.isEmpty()) {
            throw new IllegalStateException("No hay estados anteriores para deshacer en el pedido " + pedidoId + ".");
        }
        return pila.pop();
    }

    public synchronized boolean puedeDeshacer(int pedidoId) {
        Deque<PedidoMemento> pila = historialPorPedido.get(pedidoId);
        return pila != null && !pila.isEmpty();
    }

    public synchronized int cantidadGuardada(int pedidoId) {
        Deque<PedidoMemento> pila = historialPorPedido.get(pedidoId);
        return pila == null ? 0 : pila.size();
    }
}
