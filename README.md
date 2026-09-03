# Supermarket Management System API

![Deployed on Railway](https://img.shields.io/badge/deployed-Railway-0B0D0E?logo=railway&logoColor=white)

API REST en capas (Controller/Service/Repository) para la gestión de inventarios distribuidos, control de caja y ventas.

**Frontend:** [supermarket-front](https://github.com/guillerdguez/supermarket-front) (Angular 20) — demo en vivo: [supermarketweb.up.railway.app](https://supermarketweb.up.railway.app/)

**Demo en vivo (API):** [supermarket-back-production.up.railway.app](https://supermarket-back-production.up.railway.app)

**Swagger UI:** [supermarket-back-production.up.railway.app/api/swagger-ui/index.html](https://supermarket-back-production.up.railway.app/api/swagger-ui/index.html)

## Credenciales de prueba

Con `data.sql` corriendo (por defecto, ver más abajo), estos usuarios ya existen — tanto en local como en la demo:

| Rol | Email | Password |
| --- | --- | --- |
| Admin | `admin@supermarket.com` | `password` |
| Cajero | `cashier@supermarket.com` | `password` |

## Cómo empezar (API en 30 segundos)

Sin instalar nada, prueba el flujo real de login + llamada autenticada contra la demo:

```bash
# 1. Login — obtén el token JWT
TOKEN=$(curl -s -X POST https://supermarket-back-production.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@supermarket.com","password":"password"}' | jq -r '.token')

# 2. Úsalo en una llamada autenticada
curl -s https://supermarket-back-production.up.railway.app/api/products \
  -H "Authorization: Bearer $TOKEN"
```

## Sobre el proyecto

Backend de un sistema de gestión para supermercados con varias sucursales: control de stock por sucursal, caja registradora, ventas, pagos y transferencias entre sucursales. Está organizado en capas (Controller/Service/Repository) siguiendo Spring Boot estándar, con especial atención a la integridad de las operaciones de venta y caja: cada venta requiere una caja abierta en la sucursal, los precios se recalculan siempre en el backend y las operaciones críticas van en transacciones con rollback si algo falla a mitad.

## Características técnicas

* **Arquitectura en capas:** Controller / Service / Repository / Domain, con mapeo manual DTO-entidad.
* **Seguridad:**
  * Autenticación vía JWT (JSON Web Tokens), sin sesiones (`STATELESS`).
  * Rate limiting con Redis en login (5 intentos / 5 min), con `retryAfter` dinámico en la respuesta.
  * Blacklist de tokens en Redis para invalidar sesiones al hacer logout.
* **Gestión de caja registradora:** apertura y cierre de turnos con detección automática de diferencias (surplus/shortage). Cada venta queda vinculada a la caja abierta de la sucursal.
* **Gestión de pagos:** registro de pagos por venta con múltiples métodos (CASH, CARD, TRANSFER, OTHER) y validación de que no se supere el total de la venta.
* **Transferencias de stock entre sucursales:** flujo de aprobación `PENDING → APPROVED → COMPLETED` (o `REJECTED` / `CANCELLED`), con movimiento real de stock al completarse.
* **Reportes:** ventas (resumen, por sucursal, por producto, por cajero, comparativa entre períodos), estado de inventario y rendimiento de productos, cierres de caja con detección de discrepancias.
* **Gestión de usuarios:** CRUD completo (solo ADMIN), activar/desactivar, cambio de rol independiente, y protección para no poder desactivar al último admin activo. Endpoints de perfil propio para cualquier usuario autenticado.
* **Gestión de sucursales:** alta, baja lógica (activar/desactivar) y baja física solo si no tiene registros asociados (ventas, caja, inventario, transferencias, usuarios).
* **Auditoría:** registro de quién, cuándo y qué en operaciones críticas, consultable con filtros.
* **Notificaciones:** alertas de bajo stock por usuario, con contador de no leídas y marcado individual o masivo.
* **Transaccionalidad:** `@Transactional` en ventas, cancelaciones y movimientos de inventario.
* **Filtrado dinámico:** Specifications de JPA para productos, usuarios y logs de auditoría.
* **Containerización:** backend + MySQL + Redis con Docker Compose, arrancan todos con un solo comando.
* **Testing:** JUnit 5 y Mockito para unitarios, Testcontainers con Redis real para los de integración (rate limiting, seguridad end-to-end).

## Tech stack

| Área | Tecnología | Propósito |
| --- | --- | --- |
| **Core** | Java 17, Spring Boot 3.4 | Lógica y framework principal |
| **Persistencia** | Spring Data JPA / Hibernate | ORM y manejo de datos |
| **Base de datos** | MySQL 8.0 (prod) / H2 (test) | Almacenamiento relacional |
| **Caché / NoSQL** | Redis | Rate limiting y blacklist de tokens |
| **Seguridad** | Spring Security | RBAC (Role-Based Access Control) |
| **DevOps** | Docker Compose | Despliegue de infraestructura |
| **Docs** | OpenAPI (Swagger) | Documentación interactiva |
| **Testing** | JUnit 5, Mockito, Testcontainers | Tests unitarios e integración |

## Guía de despliegue (local)

### Prerrequisitos

* Docker Desktop activo.

### Un solo comando

`docker-compose up` ya levanta MySQL + Redis + el propio backend (esto cargará datos de prueba automáticamente):

```bash
docker compose up --build -d
```

No hace falta exportar ninguna variable de entorno para levantarlo así: `MYSQL_*`, `REDIS_*` y `JWT_EXPIRATION` tienen valores por defecto que coinciden con `docker-compose.yml`. `JWT_SECRET` sí tiene un default, pero solo pensado para desarrollo — nunca lo uses en producción.

### Alternativa: backend fuera de Docker

Si prefieres correr el backend directamente con Maven (por ejemplo para debug), levanta solo la infraestructura y arranca la app aparte:

```bash
docker compose up -d mysql redis
./mvnw spring-boot:run
```

En este caso el backend corre en el host, no dentro de la red de compose, así que usa el puerto publicado de MySQL (`MYSQL_PORT=3307`, el default de `application.properties`) en vez del puerto interno del contenedor.

## Docker

También se puede construir y correr solo la imagen del backend, apuntando a una base de datos externa:

```bash
docker build -t supermarket-back .
docker run -p 8080:8080 \
  -e MYSQL_HOST=host.docker.internal -e MYSQL_PORT=3307 \
  -e REDIS_HOST=host.docker.internal -e REDIS_PORT=6379 \
  -e JWT_SECRET=<un-secreto-propio> \
  supermarket-back
```

## Despliegue en producción

Desplegado en Railway: backend, MySQL, Redis y frontend (contenedor Docker + nginx) como cuatro servicios del mismo proyecto. Variables de entorno relevantes:

| Variable | Descripción | Default (dev) |
| --- | --- | --- |
| `PORT` | Puerto del servidor | `8080` |
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `dev` |
| `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DATABASE` / `MYSQL_USERNAME` / `MYSQL_PASSWORD` | Conexión a MySQL | `localhost` / `3307` / `supermarketdb` / `root` / `123456` |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Conexión a Redis | `localhost` / `6379` / (vacío) |
| `JWT_SECRET` | Secreto para firmar los JWT | sin default seguro — obligatorio en producción |
| `JWT_EXPIRATION` | Expiración del token (ms) | `86400000` (24h) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos, separados por coma | `http://localhost:4200,http://localhost:3000` |

`data.sql` siembra la base automáticamente en el primer arranque (`spring.sql.init.mode=always`), así que una base nueva en producción queda lista con los usuarios de prueba sin pasos manuales. Importante: como `spring.jpa.hibernate.ddl-auto=none` y no se usa Flyway, un cambio de esquema (columna o tabla nueva) no se aplica solo en una base ya existente — hay que aplicarlo a mano antes de desplegar el código que lo requiere.

## Lógica de negocio: el ciclo de venta

La clase `SaleServiceImpl` se encarga de mantener la integridad de cada venta:

1. **Auditoría automática:** se captura al usuario autenticado del contexto de seguridad para vincularlo a la venta.
2. **Verificación de caja abierta:** antes de procesar la venta, se valida que exista una caja registradora con estado `OPEN` en la sucursal. Si no la hay, la operación se rechaza.
3. **Verificación de stock:** se comprueba el stock en la sucursal específica; si falta stock de un ítem, la transacción se aborta (`InsufficientStockException`).
4. **Cálculo en el backend:** los precios se calculan siempre contra la base de datos, ignorando cualquier valor enviado por el cliente.
5. **Persistencia atómica:** cabecera y detalles se guardan juntos; si algo falla a mitad, se hace rollback del stock ya descontado.

## Endpoints principales

### Auth (`/api/auth`)
* `POST /api/auth/login` — obtención de token JWT (rate limiting: 5 intentos / 5 min). No hay auto-registro público: los usuarios los crea un ADMIN vía `POST /api/users`.
* `POST /api/auth/logout` — invalida el token actual en Redis.

### Sucursales (`/api/branches`)
* `GET /api/branches` — listado (ADMIN, MANAGER); por defecto solo activas, `includeInactive=true` trae todas.
* `GET /api/branches/{id}` — detalle de una sucursal (ADMIN, MANAGER).
* `POST /api/branches` — alta de sucursal (ADMIN).
* `PUT /api/branches/{id}` — actualizar datos de una sucursal (ADMIN).
* `PUT /api/branches/{id}/deactivate` — desactivar sucursal (ADMIN).
* `PUT /api/branches/{id}/reactivate` — reactivar sucursal (ADMIN).
* `DELETE /api/branches/{id}` — baja física, solo si no tiene ventas, caja, inventario, transferencias ni usuarios asociados (ADMIN).
* `GET /api/branches/{id}/inventory` — inventario completo de la sucursal (ADMIN, MANAGER, CASHIER).

### Inventario (`/api/inventory`)
* `GET /api/inventory/low-stock` — alertas de bajo stock globales (ADMIN, MANAGER).
* `GET /api/inventory/branches/{branchId}/low-stock` — alertas de bajo stock de una sucursal (ADMIN, MANAGER).
* `GET /api/inventory/branches/{branchId}/inventory` — inventario completo de una sucursal (ADMIN, MANAGER, CASHIER).
* `GET /api/inventory/branches/{branchId}/products/{productId}` — stock de un producto en una sucursal (ADMIN, MANAGER, CASHIER).
* `GET /api/inventory/products/{productId}/total-stock` — stock total de un producto en todas las sucursales (ADMIN, MANAGER).
* `PUT /api/inventory/branches/{branchId}/products/{productId}` — fija stock y stock mínimo de un producto en una sucursal (ADMIN, MANAGER).
* `PATCH /api/inventory/branches/{branchId}/products/{productId}/adjust` — ajusta el stock sumando/restando un delta (ADMIN, MANAGER).

### Productos (`/api/products`)
* `GET /api/products` — búsqueda con filtros (`name`, `category`, `price`).

### Transacciones (`/api/sales`, `/api/cashier`)
* `POST /api/sales` — procesar nueva venta (requiere caja abierta en la sucursal).
* `POST /api/sales/{id}/cancel` — anulación con motivo: revierte el stock automáticamente (ADMIN, MANAGER).
* `GET /api/cashier/my-sales` — historial de ventas del cajero autenticado.
* `GET /api/cashier/my-sales/{id}` — detalle de una venta propia del cajero autenticado.

### Caja registradora (`/api/cash-registers`)
* `POST /api/cash-registers/open` — apertura de turno con saldo inicial.
* `POST /api/cash-registers/{id}/close` — cierre con saldo final.
* `GET /api/cash-registers/branches/{branchId}/current` — caja activa de una sucursal.

### Pagos (`/api/payments`)
* `POST /api/payments` — registrar pago para una venta (valida que no supere el total).
* `GET /api/payments/sale/{saleId}` — pagos de una venta.

### Transferencias de stock (`/api/transfers`)
* `POST /api/transfers` — solicitar transferencia entre sucursales.
* `GET /api/transfers` — listar todas (ADMIN, MANAGER).
* `GET /api/transfers/mine` — transferencias solicitadas por el usuario actual.
* `GET /api/transfers/{id}` — detalle de una transferencia.
* `POST /api/transfers/{id}/approve` — aprobar solicitud pendiente (ADMIN, MANAGER).
* `POST /api/transfers/{id}/reject` — rechazar con motivo (ADMIN, MANAGER).
* `POST /api/transfers/{id}/complete` — ejecutar movimiento real de stock (ADMIN, MANAGER).
* `POST /api/transfers/{id}/cancel` — cancelar (solicitante o ADMIN).
* `GET /api/transfers/status/{status}` — filtrar por estado.
* `GET /api/transfers/source/{branchId}` — filtrar por sucursal de origen (ADMIN, MANAGER).
* `GET /api/transfers/target/{branchId}` — filtrar por sucursal de destino (ADMIN, MANAGER).

### Reportes (`/api/reports`) — ADMIN, MANAGER
* `GET /api/reports/sales/summary` — resumen global de ventas con filtros.
* `GET /api/reports/sales/by-branch` — ventas agrupadas por sucursal.
* `GET /api/reports/sales/by-product` — ventas por producto.
* `GET /api/reports/sales/by-cashier` — rendimiento por cajero con ticket promedio.
* `GET /api/reports/sales/comparison` — comparativa del período actual vs. el anterior.
* `GET /api/reports/inventory/status` — estado global del inventario.
* `GET /api/reports/inventory/performance` — rendimiento de productos con tasa de rotación.
* `GET /api/reports/cash-registers` — reporte de cierres con detección de discrepancias.

### Usuarios (`/api/users`) — ADMIN
* `GET /api/users` — lista con filtros (username, email, rol).
* `GET /api/users/{id}` — detalle de un usuario.
* `POST /api/users` — crear usuario con cualquier rol.
* `PUT /api/users/{id}` — actualizar datos de un usuario.
* `PUT /api/users/{id}/role` — cambiar rol de un usuario.
* `DELETE /api/users/{id}` — desactivación lógica; bloqueada si es el último admin activo.
* `PUT /api/users/{id}/activate` — reactivar un usuario desactivado.

### Auditoría (`/api/audit-logs`) — ADMIN
* `GET /api/audit-logs` — listado con filtros (username, action, status, rango de fechas).
* `GET /api/audit-logs/{id}` — detalle de un log de auditoría.

### Notificaciones (`/api/notifications`)
* `GET /api/notifications` — notificaciones no leídas del usuario actual.
* `GET /api/notifications/all` — todas las notificaciones del usuario actual.
* `GET /api/notifications/count` — contador de no leídas.
* `PUT /api/notifications/{id}/read` — marcar una notificación como leída.
* `DELETE /api/notifications/{id}` — eliminar una notificación.
* `PUT /api/notifications/mark-all-read` — marcar todas como leídas.

### Perfil propio (`/api/profile`)
* `GET /api/profile` — ver perfil del usuario autenticado.
* `PUT /api/profile` — actualizar username, nombre y apellido.
* `POST /api/profile/change-password` — cambiar contraseña con validación de la actual.

## Ejemplo de venta (payload)

**Request (`POST /api/sales`):**
```json
{
  "branchId": 1,
  "date": "2026-02-25",
  "details": [
    { "productId": 10, "quantity": 2 },
    { "productId": 5, "quantity": 1 }
  ]
}
```

**Respuesta (201 Created):**
```json
{
  "id": 125,
  "total": 3500.00,
  "status": "REGISTERED",
  "cashRegisterId": 10,
  "cashRegisterStatus": "OPEN",
  "createdByUsername": "cashier1",
  "createdAt": "2026-02-25 10:30:00",
  "details": [
    { "productName": "Whole Milk 1L", "quantity": 2, "unitPrice": 1200.50, "subtotal": 2401.00 },
    { "productName": "Mineral Water 1.5L", "quantity": 1, "unitPrice": 800.00, "subtotal": 800.00 }
  ]
}
```

## Accesos

| Recurso | Local | Producción (Railway) |
| --- | --- | --- |
| **Swagger UI** | `http://localhost:8080/api/swagger-ui/index.html` | [supermarket-back-production.up.railway.app/api/swagger-ui/index.html](https://supermarket-back-production.up.railway.app/api/swagger-ui/index.html) |
| **Docs JSON** | `http://localhost:8080/api/v3/api-docs` | `https://supermarket-back-production.up.railway.app/api/v3/api-docs` |
| **DB (MySQL)** | `jdbc:mysql://localhost:3307/supermarketdb` | gestionada por Railway, no expuesta públicamente |

---

**Autor:** Guillermo — Java Backend Developer
