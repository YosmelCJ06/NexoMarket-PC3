package pe.nexomarket.domain.catalogo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Patrón COMPOSITE — participante "Composite": una categoría que contiene
 * productos y también otras subcategorías. Sus totales se calculan
 * recorriendo a sus hijos, sin importar si son hojas o categorías.
 */
public class CategoriaComposite implements NodoCatalogo {
    private final String nombre;
    private final List<NodoCatalogo> hijos = new ArrayList<>();

    public CategoriaComposite(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public CategoriaComposite agregar(NodoCatalogo nodo) {
        hijos.add(Objects.requireNonNull(nodo));
        return this;
    }

    public void quitar(NodoCatalogo nodo) {
        hijos.remove(nodo);
    }

    public List<NodoCatalogo> getHijos() {
        return List.copyOf(hijos);
    }

    @Override
    public String obtenerNombre() {
        return nombre;
    }

    @Override
    public BigDecimal obtenerPrecioTotal() {
        return hijos.stream().map(NodoCatalogo::obtenerPrecioTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int contarProductos() {
        return hijos.stream().mapToInt(NodoCatalogo::contarProductos).sum();
    }

    @Override
    public int obtenerStockTotal() {
        return hijos.stream().mapToInt(NodoCatalogo::obtenerStockTotal).sum();
    }

    @Override
    public List<ProductoHoja> obtenerProductos() {
        return hijos.stream().flatMap(hijo -> hijo.obtenerProductos().stream()).toList();
    }
}
