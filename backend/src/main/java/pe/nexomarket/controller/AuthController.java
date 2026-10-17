package pe.nexomarket.controller;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        if (request == null || request.email() == null || request.password() == null) {
            return unauthorized();
        }

        List<AccountRecord> accounts = jdbcTemplate.query("""
                SELECT u.id_usuario, u.email, u.password_hash, u.activo,
                       r.nombre_rol, COALESCE(c.nombre, u.email) AS nombre
                FROM usuario u
                JOIN rol r ON r.id_rol = u.id_rol
                LEFT JOIN cliente c ON c.id_cliente = u.id_cliente
                WHERE LOWER(u.email) = LOWER(?)
                """, (resultSet, rowNumber) -> new AccountRecord(
                resultSet.getInt("id_usuario"),
                resultSet.getString("email"),
                resultSet.getString("password_hash"),
                resultSet.getBoolean("activo"),
                resultSet.getString("nombre_rol"),
                resultSet.getString("nombre")), request.email().trim());

        if (accounts.isEmpty()) return unauthorized();
        AccountRecord account = accounts.get(0);
        if (!account.active() || !passwordEncoder.matches(request.password(), account.passwordHash())) {
            return unauthorized();
        }

        String roleCode = account.role().toUpperCase(Locale.ROOT);
        if (!roleCode.equals("CLIENTE") && !roleCode.equals("ADMINISTRADOR")) return unauthorized();

        session.setAttribute("nexomarket.userId", account.id());
        session.setAttribute("nexomarket.email", account.email());
        session.setAttribute("nexomarket.name", account.name());
        session.setAttribute("nexomarket.role", roleCode);
        return ResponseEntity.ok(sessionResponse(account.id(), account.email(), account.name(), roleCode));
    }

    @GetMapping("/session")
    public ResponseEntity<?> currentSession(HttpSession session) {
        Integer id = (Integer) session.getAttribute("nexomarket.userId");
        String email = (String) session.getAttribute("nexomarket.email");
        String name = (String) session.getAttribute("nexomarket.name");
        String roleCode = (String) session.getAttribute("nexomarket.role");
        if (id == null || email == null || name == null || roleCode == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(sessionResponse(id, email, name, roleCode));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()
                || request.email() == null || !request.email().contains("@")
                || request.password() == null || request.password().length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("message", "Revisa nombre, correo y contraseña (mínimo 8 caracteres)."));
        }

        String name = request.name().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Integer existingUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuario WHERE LOWER(email) = LOWER(?)", Integer.class, email);
        if (existingUsers != null && existingUsers > 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Ya existe una cuenta con ese correo."));
        }

        try {
            Integer clientId = jdbcTemplate.queryForObject("""
                    INSERT INTO cliente (nombre, email)
                    VALUES (?, ?)
                    RETURNING id_cliente
                    """, Integer.class, name, email);
            jdbcTemplate.queryForObject("""
                    INSERT INTO usuario (id_rol, id_cliente, email, password_hash)
                    SELECT id_rol, ?, ?, ?
                    FROM rol
                    WHERE nombre_rol = 'CLIENTE'
                    RETURNING id_usuario
                    """, Integer.class, clientId, email, passwordEncoder.encode(request.password()));
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Cuenta de cliente creada. Ya puedes iniciar sesión."));
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Correo o contraseña incorrectos, o cuenta inactiva."));
    }

    private SessionResponse sessionResponse(int id, String email, String name, String roleCode) {
        String role = roleCode.equals("ADMINISTRADOR") ? "Administrador" : "Cliente";
        return new SessionResponse(id, email, name, role, roleCode);
    }

    private record AccountRecord(int id, String email, String passwordHash, boolean active, String role, String name) {
    }

    public record LoginRequest(String email, String password) {
    }

    public record RegisterRequest(String name, String email, String password) {
    }

    public record SessionResponse(int id, String email, String name, String role, String roleCode) {
    }
}
