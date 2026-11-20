# NexoMarket – PC2 Diseño de Patrones

Proyecto demostrativo de patrones y tienda web, estructurado a partir del código incluido en `Proyecto PC2.docx`.

## Requisitos

- JDK 21.
- Node.js 18 o superior solo para la demo de patrones y sus pruebas.

## Ejecutar en Windows

1. Extraer el ZIP.
2. Abrir una terminal **dentro de la carpeta que contiene `package.json`**.
3. Ejecutar la tienda web con Spring Boot y JDBC:

```powershell
.\backend\mvnw.cmd -f .\backend\pom.xml spring-boot:run
```

Visita `http://localhost:4173`. No se necesita `node_modules` para la tienda.

Para ejecutar la demostración de patrones en consola:

```bash
npm start
```

También funciona: `node src/main.js`.

Para ejecutar las pruebas de patrones:

```bash
npm test
```

## Conexión local a PostgreSQL

1. Copiar `.env.example` como `.env`.
2. Confirmar en `.env` los valores de `DB_HOST`, `DB_PORT`, `DB_NAME` y `DB_USER`.
3. Escribir la contraseña local en `DB_PASSWORD`. No compartir ni subir `.env`.
4. Iniciar o reiniciar Spring con `backend\mvnw.cmd -f backend\pom.xml spring-boot:run` y abrir `http://localhost:4173/api/health`.

La ruta responde `status: ok` cuando conecta a PostgreSQL. Spring lee `.env`; el navegador nunca recibe la contraseña.

El catálogo usa `GET /api/products`. El panel guarda productos con `POST /api/products` y actualiza existencias con `PUT /api/products/{id}/stock`. Las categorías se crean al guardar un producto si todavía no existen.

La vista «Mis pedidos» consulta `GET /api/orders` y filtra por estado con `GET /api/orders?estado=ENVIADO`. Spring limita cada resultado a los pedidos del usuario autenticado.

El login consulta `usuario` y `rol`; el registro crea un `cliente` y su usuario con contraseña BCrypt y rol `CLIENTE`. Las escrituras de inventario requieren una sesión de servidor con rol `ADMINISTRADOR`.

## Estructura

- `src/creacionales`: Singleton y fábrica simple de pagos.
- `src/estructurales`: Adapter, Facade y Decorator.
- `src/comportamiento`: Strategy (métodos de pago) y Observer (notificación de estados).
- `src/main.js`: demostración de los patrones en consola.
- `backend/`: servidor Spring Boot, JDBC y driver PostgreSQL.
- `backend/src/main/java/pe/nexomarket/controller/AuthController.java`: registro, login, sesión HTTP y cierre de sesión con roles de PostgreSQL.
- `backend/src/main/java/pe/nexomarket/controller/ProductController.java`: lectura y mantenimiento del catálogo en PostgreSQL.
- `backend/src/main/java/pe/nexomarket/controller/OrderController.java`: consulta y filtro de pedidos por cliente autenticado.
- `public/`: interfaz demostrativa de cliente y administrador.
- `tests/`: pruebas automatizadas con el módulo integrado `node:test`.

## Alcance y limitaciones

- No es una tienda de producción ni usa NestJS.
- El catálogo y el inventario se leen y guardan en PostgreSQL. La base debe tener las tablas del esquema y productos para que aparezcan en la tienda.
- Las cuentas de Cliente y el login usan PostgreSQL. El administrador inicial debe existir en `usuario`, tener rol `ADMINISTRADOR` y un `password_hash` BCrypt; no hay credenciales admin predeterminadas.
- El historial y filtro de pedidos leen PostgreSQL; la creación de pedidos desde checkout y los pagos siguen en modo demostración y todavía no se guardan en la base.
- Los pagos y la API de Olva Courier son simulaciones; no se cobran pagos ni se consulta un proveedor real.
- La clase `PagoFactory` es una fábrica simple y no el patrón Factory Method GoF estricto.
- Faltan los patrones requeridos por la rúbrica: **Prototype, Abstract Factory, Builder y Composite**.
- Se hicieron ajustes mínimos para ejecutar con módulos ES y proteger el constructor del Singleton; se añadieron validaciones y pruebas.
- Usa `backend\mvnw.cmd -f backend\pom.xml spring-boot:run` para la web, `npm start` para la consola y `npm test` para las pruebas de patrones.

Esta entrega sirve para **correr y estudiar el código actual**, no acredita por sí sola el cumplimiento íntegro de la rúbrica de 20 puntos.
