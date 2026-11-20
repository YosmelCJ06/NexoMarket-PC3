package pe.nexomarket.domain.pedido;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import pe.nexomarket.domain.pedido.observer.EventoPedido;
import pe.nexomarket.domain.pedido.observer.ObservadorPedido;

/**
 * Entidad Pedido. Cumple tres roles:
 * <ul>
 *   <li>STATE — "Context": delega cada acción en su {@link EstadoPedido} actual.</li>
 *   <li>OBSERVER — "Subject": avisa a sus {@link ObservadorPedido} cuando cambia de estado.</li>
 *   <li>MEMENTO — "Originator": crea y restaura {@link PedidoMemento}.</li>
 * </ul>
 */
public class Pedido {
    private final int id;
    private final int clienteId;
    private BigDecimal montoTotal;
    private EstadoPedido estadoActual;
    private final List<ObservadorPedido> observadores = new ArrayList<>();

    /** Pedido nuevo: siempre empieza en PENDIENTE. */
    public Pedido(int id, int clienteId, BigDecimal montoTotal) {
        this(id, clienteId, montoTotal, new EstadoPendiente());
    }

    private Pedido(int id, int clienteId, BigDecimal montoTotal, EstadoPedido estado) {
        if (id <= 0) throw new IllegalArgumentException("El id del pedido debe ser positivo.");
        if (clienteId <= 0) throw new IllegalArgumentException("El cliente del pedido es obligatorio.");
        if (montoTotal == null || montoTotal.signum() < 0) {
            throw new IllegalArgumentException("El monto del pedido no puede ser negativo.");
        }
        this.id = id;
        this.clienteId = clienteId;
        this.montoTotal = montoTotal;
        this.estadoActual = Objects.requireNonNull(estado);
    }

    /** Reconstruye un pedido leído de la base de datos con su estado guardado. */
    public static Pedido reconstruir(int id, int clienteId, BigDecimal montoTotal, String codigoEstado) {
        return new Pedido(id, clienteId, montoTotal, EstadosPedido.desdeCodigo(codigoEstado));
    }

    // ---------- OBSERVER: gestión de suscriptores ----------

    public void suscribir(ObservadorPedido observador) {
        observadores.add(Objects.requireNonNull(observador));
    }

    public void desuscribir(ObservadorPedido observador) {
        observadores.remove(observador);
    }

    private void notificar(String estadoAnterior, String detalle) {
        EventoPedido evento = new EventoPedido(id, getCodigo(), estadoAnterior, getNombreEstado(), detalle);
        for (ObservadorPedido observador : List.copyOf(observadores)) {
            observador.actualizar(evento);
        }
    }

    // ---------- STATE: acciones delegadas al estado actual ----------

    public void pagar() {
        estadoActual.pagar(this);
    }

    public void preparar() {
        estadoActual.preparar(this);
    }

    public void enviar() {
        estadoActual.enviar(this);
    }

    public void entregar() {
        estadoActual.entregar(this);
    }

    public void cancelar() {
        estadoActual.cancelar(this);
    }

    /** Solo los estados (mismo paquete) pueden cambiar el estado del pedido. */
    void cambiarEstado(EstadoPedido nuevoEstado, String detalle) {
        String anterior = estadoActual.getNombreEstado();
        estadoActual = Objects.requireNonNull(nuevoEstado);
        notificar(anterior, detalle);
    }

    // ---------- MEMENTO: guardar y restaurar ----------

    public PedidoMemento crearMemento() {
        return new PedidoMemento(id, estadoActual, montoTotal);
    }

    public void restaurar(PedidoMemento memento) {
        Objects.requireNonNull(memento, "No hay un estado guardado para restaurar.");
        if (memento.pedidoId() != id) {
            throw new IllegalArgumentException("El memento no pertenece al pedido " + getCodigo() + ".");
        }
        String anterior = estadoActual.getNombreEstado();
        estadoActual = memento.estado();
        montoTotal = memento.montoTotal();
        notificar(anterior, "Estado restaurado con Memento (deshacer).");
    }

    // ---------- Consultas ----------

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return String.format("PED-%05d", id);
    }

    public int getClienteId() {
        return clienteId;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public String getNombreEstado() {
        return estadoActual.getNombreEstado();
    }

    public boolean estaFinalizado() {
        return estadoActual.esFinal();
    }
}
