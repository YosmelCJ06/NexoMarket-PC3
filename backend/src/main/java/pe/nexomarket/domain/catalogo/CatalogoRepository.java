package pe.nexomarket.domain.catalogo;

/** Puerto para obtener el catálogo completo como árbol Composite (categorías con productos). */
public interface CatalogoRepository {
    CategoriaComposite cargarCatalogo();
}
