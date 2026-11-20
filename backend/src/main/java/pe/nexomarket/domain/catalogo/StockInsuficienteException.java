package pe.nexomarket.domain.catalogo;

/** No hay unidades suficientes de un producto para atender la compra. */
public class StockInsuficienteException extends IllegalStateException {
    public StockInsuficienteException(String producto, int pedidas) {
        super("No hay stock suficiente de " + producto + " para " + pedidas + " unidad(es).");
    }
}
