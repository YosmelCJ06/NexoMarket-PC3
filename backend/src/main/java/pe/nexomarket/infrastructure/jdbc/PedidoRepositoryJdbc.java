package pe.nexomarket.infrastructure.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
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
