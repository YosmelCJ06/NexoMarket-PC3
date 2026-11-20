package pe.nexomarket.controller;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pe.nexomarket.application.checkout.CheckoutFacade;
import pe.nexomarket.application.checkout.ItemCarrito;
import pe.nexomarket.application.pedido.consulta.DetallePedido;
import pe.nexomarket.web.SesionHttp;

/** POST /api/orders: el cliente confirma su compra a través del CheckoutFacade. */
@RestController
public class CheckoutController {
    private final CheckoutFacade checkout;

    public CheckoutController(CheckoutFacade checkout) {
        this.checkout = checkout;
    }

    @PostMapping("/api/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public DetallePedido comprar(@RequestBody CompraRequest compra, HttpSession sesion) {
        if (compra == null) throw new IllegalArgumentException("Faltan los datos de la compra.");
        return checkout.comprar(SesionHttp.usuario(sesion), compra.items(), compra.metodoPago());
    }

    public record CompraRequest(List<ItemCarrito> items, String metodoPago) {
    }
}
