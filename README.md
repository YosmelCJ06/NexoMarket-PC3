# NexoMarket – PC3 Diseño de Patrones

Tienda en línea (catálogo, pedidos, pagos simulados y reportes) hecha con **Spring Boot + JDBC + PostgreSQL** y una interfaz web en HTML/JavaScript. En la PC3 se agregan los patrones **Proxy, Bridge, State, Observer, Command y Memento**, además de **Composite**, todos conectados a la API y a la web.

## Requisitos

- JDK 21 o superior.
- PostgreSQL 14 o superior (local o en un servicio como Supabase).
- Node.js 18 o superior, solo para la demo en consola de los patrones de la PC2.

## 1. Crear la base de datos

```powershell
psql -U postgres -c "CREATE DATABASE \"NexoMarket\";"
psql -U postgres -d NexoMarket -f database/schema.sql
psql -U postgres -d NexoMarket -f database/datos_demo.sql
```

También se puede hacer desde pgAdmin: crear la base `NexoMarket`, abrir *Query Tool* y ejecutar los dos archivos en ese orden.

`datos_demo.sql` crea roles, categorías, 6 productos, 3 pedidos de ejemplo y **usuarios de prueba** (las contraseñas están escritas al inicio del archivo). Son solo para desarrollo y la demo.

## 2. Configurar las variables de entorno

1. Copiar `.env.example` como `.env` en la carpeta raíz (la que tiene `package.json`).
2. Completar `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD`.
3. `PORT` define el puerto de la web (por defecto `4173`).

`.env` no se sube al repositorio. Spring lo lee al arrancar; el navegador nunca recibe la contraseña.

## 3. Ejecutar

Desde la carpeta raíz del proyecto:

```powershell
.\backend\mvnw.cmd -f .\backend\pom.xml spring-boot:run
```

Abrir `http://localhost:4173`. La ruta `http://localhost:4173/api/health` responde `status: ok` cuando hay conexión con PostgreSQL.

## 4. Pruebas

```powershell
.\backend\mvnw.cmd -f .\backend\pom.xml test
```

Ejecuta las pruebas JUnit de todos los patrones, de la API (MockMvc) y de los repositorios JDBC. Estas últimas corren `database/schema.sql` y `database/datos_demo.sql` sobre H2 en modo PostgreSQL, así que **no necesitan una base de datos instalada**.

Demo y pruebas en consola de los patrones de la PC2 (JavaScript): `npm start` y `npm test`.

## Patrones y dónde verlos

| Patrón | Clases principales (backend) | Dónde se ve en la web / API |
|---|---|---|
| **State** | `domain/pedido`: `Pedido`, `EstadoPedido`, `EstadoPendiente` … `EstadoCancelado` | Botones Pagar, Preparar, Enviar, Entregar y Cancelar del panel de administrador |
| **Observer** | `domain/pedido/observer`: `AuditoriaPedidoObservador`, `NotificacionClienteObservador` | Botón *Historial* (auditoría) y log `[EMAIL SIMULADO]` del servidor |
| **Command** | `application/pedido/comando`: `InvocadorComandos`, `PagarPedidoCommand` … | Cada botón de acción; registro de comandos en *Historial* |
| **Memento** | `PedidoMemento`, `memento/HistorialPedidoCaretaker` | Botón *Deshacer* |
| **Proxy** | `application/pedido/consulta`: `PedidoServiceProxy` | «Mis pedidos» → *Consultar un pedido por número* |
| **Bridge** | `application/reporte`: `Exportable`, `FormatoHTMLExport`, `FormatoCSVExport` | Sección *Reportes* del panel de administrador |
| **Composite** | `domain/catalogo`: `CategoriaComposite`, `ProductoHoja` | Reporte del catálogo |
| **Facade + Strategy** (PC2, refactorizados) | `application/checkout`: `CheckoutFacade`, `ServicioInventario`, `ServicioPago`, `ServicioPedido`, `pago/*` | Confirmar compra desde la bolsa |

## API

| Método y ruta | Descripción | Acceso |
|---|---|---|
| `POST /api/auth/login` · `POST /api/auth/register` · `GET /api/auth/session` · `POST /api/auth/logout` | Sesión | Público |
| `GET /api/products` · `POST /api/products` · `PUT /api/products/{id}/stock` | Catálogo e inventario | Lectura pública; escritura administrador |
| `GET /api/orders?estado=…` | Lista de pedidos (el cliente solo ve los suyos) | Cliente o administrador |
| `POST /api/orders` `{"items":[{"productoId":1,"cantidad":1}],"metodoPago":"YAPE"}` | Compra (Facade) | Cliente |
| `GET /api/orders/{id}` | Detalle protegido por el Proxy | Dueño o administrador |
| `POST /api/orders/{id}/acciones` `{"accion":"pagar"}` | Command + State + Observer | Administrador |
| `POST /api/orders/{id}/deshacer` | Memento | Administrador |
| `GET /api/orders/{id}/historial` | Auditoría y comandos | Dueño o administrador |
| `GET /api/reports/catalog?formato=html\|csv` · `GET /api/reports/orders?formato=html\|csv` | Bridge + Composite | Administrador |

Los errores se devuelven como `{"message": "..."}` con código 400, 403, 404, 409 o 503.

## Estructura

- `backend/`: Spring Boot. Paquetes `domain` (entidades y patrones), `application` (casos de uso), `infrastructure` (JDBC y memoria), `controller` (API) y `config`.
- `database/`: `schema.sql` y `datos_demo.sql`.
- `public/`: interfaz web de cliente y administrador.
- `docs/uml/`: diagramas UML en PlantUML (`.puml`) y sus imágenes PNG.
- `src/` y `tests/`: demo en JavaScript de los patrones de la PC2 y sus pruebas.

## Ramas

- `main`: versión estable. La entrega de la PC2 está marcada con la etiqueta `pc2-base`.
- `develop`: integración de la PC3.
- `feature/*`: una rama por funcionalidad, que se fusiona en `develop` con sus pruebas pasando.

## Limitaciones

- Los pagos (tarjeta y Yape) y la notificación por correo son **simulados**: no se cobra ni se envía nada real.
- `schema.sql` se reconstruyó a partir de las consultas del backend. Si se usa una base creada antes, debe tener las mismas tablas y columnas, y la columna `pedido.estado` debe aceptar `PAGADO`.
- El historial de comandos y mementos se guarda en memoria del servidor: se pierde al reiniciar.
