package pe.nexomarket.infrastructure.jdbc;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import pe.nexomarket.domain.pedido.EstadoPendiente;
import pe.nexomarket.domain.pedido.LineaPedido;
import org.springframework.stereotype.Repository;
import pe.nexomarket.domain.pedido.Pedido;
import pe.nexomarket.domain.pedido.PedidoNoEncontradoException;
import pe.nexomarket.domain.pedido.PedidoRepository;

/** Implementación del repositorio de pedidos sobre las tablas pedido y cliente de PostgreSQL. */
@Repository
public class PedidoRepositoryJdbc implements PedidoRepository {
    private static final String SELECT_PEDIDO = """
            SELECT id_pedido, id_cliente, monto_total, estado
            FROM pedido
            """;

    private final JdbcTemplate jdbcTemplate;

    public PedidoRepositoryJdbc(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Pedido> buscarPorId(int pedidoId) {
        return jdbcTemplate.query(SELECT_PEDIDO + " WHERE id_pedido = ?", this::mapear, pedidoId).stream().findFirst();
    }

    @Override
    public List<Pedido> listar() {
        return jdbcTemplate.query(SELECT_PEDIDO + " ORDER BY id_pedido", this::mapear);
    }

    @Override
    public Pedido crear(int clienteId, List<LineaPedido> lineas) {
        if (lineas == null || lineas.isEmpty()) throw new IllegalArgumentException("El pedido no tiene productos.");
        BigDecimal total = lineas.stream().map(LineaPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        GeneratedKeyHolder llave = new GeneratedKeyHolder();
        jdbcTemplate.update(conexion -> {
            PreparedStatement sentencia = conexion.prepareStatement(
                    "INSERT INTO pedido (id_cliente, monto_total, estado) VALUES (?, ?, ?)", new String[] {"id_pedido"});
            sentencia.setInt(1, clienteId);
            sentencia.setBigDecimal(2, total);
            sentencia.setString(3, EstadoPendiente.CODIGO);   // todo pedido nuevo empieza en PENDIENTE (State)
            return sentencia;
        }, llave);
        int pedidoId = Objects.requireNonNull(llave.getKey(), "PostgreSQL no devolvió el id del pedido.").intValue();
        jdbcTemplate.batchUpdate(
                "INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) VALUES (?, ?, ?, ?)",
                lineas, lineas.size(), (sentencia, linea) -> {
                    sentencia.setInt(1, pedidoId);
                    sentencia.setInt(2, linea.productoId());
                    sentencia.setInt(3, linea.cantidad());
                    sentencia.setBigDecimal(4, linea.precioUnitario());
                });
        return new Pedido(pedidoId, clienteId, total);
    }

    @Override
    public void guardar(Pedido pedido) {
        int filas = jdbcTemplate.update("UPDATE pedido SET estado = ?, monto_total = ? WHERE id_pedido = ?",
                pedido.getNombreEstado(), pedido.getMontoTotal(), pedido.getId());
        if (filas == 0) throw new PedidoNoEncontradoException(pedido.getId());
    }

    @Override
    public Optional<String> buscarCorreoCliente(int clienteId) {
        return jdbcTemplate.queryForList("SELECT email FROM cliente WHERE id_cliente = ?", String.class, clienteId)
                .stream().findFirst();
    }

    private Pedido mapear(ResultSet fila, int numero) throws SQLException {
        return Pedido.reconstruir(fila.getInt("id_pedido"), fila.getInt("id_cliente"),
                fila.getBigDecimal("monto_total"), fila.getString("estado"));
    }
}
