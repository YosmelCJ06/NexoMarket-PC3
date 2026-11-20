-- =====================================================================
-- NexoMarket - Datos de demostración (ejecutar después de schema.sql)
--
-- Usuarios de PRUEBA (solo para desarrollo y la demo de la PC3):
--   admin@nexomarket.pe    / Admin2026!    (rol ADMINISTRADOR)
--   ana@nexomarket.pe      / Cliente2026!  (rol CLIENTE, dueña de los pedidos 1 y 2)
--   luis@nexomarket.pe     / Cliente2026!  (rol CLIENTE, dueño del pedido 3)
-- Las contraseñas se guardan con BCrypt, igual que en el registro de la aplicación.
-- =====================================================================

INSERT INTO rol (nombre_rol) VALUES ('ADMINISTRADOR'), ('CLIENTE');

INSERT INTO cliente (nombre, email) VALUES
    ('Ana Torres', 'ana@nexomarket.pe'),
    ('Luis Ramos', 'luis@nexomarket.pe');

INSERT INTO usuario (id_rol, id_cliente, email, password_hash) VALUES
    ((SELECT id_rol FROM rol WHERE nombre_rol = 'ADMINISTRADOR'), NULL, 'admin@nexomarket.pe',
     '$2a$10$UzeIZubED8z.FJZdI5vovugrdR4/jYHsz5et84tX46cvu4n3gUC/K'),
    ((SELECT id_rol FROM rol WHERE nombre_rol = 'CLIENTE'),
     (SELECT id_cliente FROM cliente WHERE email = 'ana@nexomarket.pe'), 'ana@nexomarket.pe',
     '$2a$10$.y088s6x5p5II1WTRx8WQeH4Wy/3uMZR.e1rpOEOZmmSU1dydnGdq'),
    ((SELECT id_rol FROM rol WHERE nombre_rol = 'CLIENTE'),
     (SELECT id_cliente FROM cliente WHERE email = 'luis@nexomarket.pe'), 'luis@nexomarket.pe',
     '$2a$10$wi684XtfPetSAD9UtIMruuXkDgm0.n20SjPSUgsmo6ND0vUHj9EXO');

INSERT INTO categoria (nombre_categoria) VALUES ('Computadoras'), ('Celulares'), ('Audio'), ('Accesorios');

INSERT INTO producto (id_categoria, nombre, precio, stock) VALUES
    ((SELECT id_categoria FROM categoria WHERE nombre_categoria = 'Computadoras'), 'MacBook Pro 14', 7499.90, 8),
    ((SELECT id_categoria FROM categoria WHERE nombre_categoria = 'Computadoras'), 'Laptop Lenovo IdeaPad 5', 2399.00, 12),
    ((SELECT id_categoria FROM categoria WHERE nombre_categoria = 'Celulares'), 'Samsung Galaxy S24', 3299.00, 15),
    ((SELECT id_categoria FROM categoria WHERE nombre_categoria = 'Celulares'), 'iPhone 15', 3999.00, 4),
    ((SELECT id_categoria FROM categoria WHERE nombre_categoria = 'Audio'), 'Audífonos Sony WH-1000XM5', 1299.00, 10),
    ((SELECT id_categoria FROM categoria WHERE nombre_categoria = 'Accesorios'), 'Mouse Logitech MX Master 3S', 389.90, 30);

-- Pedidos de ejemplo para probar State, Command, Memento y Proxy
INSERT INTO pedido (id_cliente, monto_total, estado) VALUES
    ((SELECT id_cliente FROM cliente WHERE email = 'ana@nexomarket.pe'), 7889.80, 'PENDIENTE'),
    ((SELECT id_cliente FROM cliente WHERE email = 'ana@nexomarket.pe'), 1299.00, 'PAGADO'),
    ((SELECT id_cliente FROM cliente WHERE email = 'luis@nexomarket.pe'), 3299.00, 'PREPARANDO');

INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario) VALUES
    (1, (SELECT id_producto FROM producto WHERE nombre = 'MacBook Pro 14'), 1, 7499.90),
    (1, (SELECT id_producto FROM producto WHERE nombre = 'Mouse Logitech MX Master 3S'), 1, 389.90),
    (2, (SELECT id_producto FROM producto WHERE nombre = 'Audífonos Sony WH-1000XM5'), 1, 1299.00),
    (3, (SELECT id_producto FROM producto WHERE nombre = 'Samsung Galaxy S24'), 1, 3299.00);
