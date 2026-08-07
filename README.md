# 🛒 Supermarket Management System (SMS) API

**Enterprise-Grade REST API** diseñada para la gestión integral de inventarios distribuidos, control de caja y ventas seguras.

**Frontend:** [supermarket-front](https://github.com/guillerdguez/supermarket-front) (Angular 20).

<!-- 🔗 Demo en vivo: pendiente — ver "Despliegue" más abajo -->

## 📸 Capturas

El frontend conectado a esta API — ver [supermarket-front](https://github.com/guillerdguez/supermarket-front#-capturas) para las capturas de pantalla.

## 🔑 Credenciales de prueba

Con `data.sql` corriendo (por defecto, ver más abajo), estos usuarios ya existen:

| Rol | Email | Password |
| --- | --- | --- |
| Admin | `admin@supermarket.com` | `password` |
| Cajero | `cashier@supermarket.com` | `password` |

## 💡 Sobre el Proyecto

Este proyecto es el núcleo backend de un sistema ERP para retail. Ha sido diseñado siguiendo principios de **Clean Architecture y SOLID**, priorizando la integridad financiera, la trazabilidad de operaciones y la seguridad.

El sistema resuelve el problema de la gestión de stock en múltiples sucursales y asegura que cada transacción sea atómica, auditable y esté vinculada a una caja registradora abierta.

## ✨ Características Técnicas Implementadas

Este backend implementa lógica de negocio compleja más allá de un simple CRUD:

* 🏗️ **Arquitectura Robusta:** Diseño modular en capas (`Controller`, `Service`, `Repository`, `Domain`).
* 🔐 **Seguridad Avanzada:**
  * Autenticación vía **JWT** (JSON Web Tokens).
  * **Rate Limiting** con Redis para prevenir fuerza bruta, con `retryAfter` dinámico en la respuesta.
  * **Token Blacklist** para invalidación real de sesiones al hacer Logout.
* 💼 **Gestión de Caja Registradora:** Apertura y cierre de turnos con detección automática de diferencias (surplus/shortage). Cada venta queda vinculada a la caja abierta de la sucursal.
* 💳 **Gestión de Pagos:** Registro de pagos por venta con soporte para múltiples métodos (CASH, CARD, TRANSFER, OTHER) y validación de límite por total de venta.
* 🔄 **Transferencias de Stock entre Sucursales:** Flujo completo de aprobación: `PENDING → APPROVED → COMPLETED` (o `REJECTED / CANCELLED`). Mueve stock real al completarse.
* 📊 **Reportes de Negocio:** Endpoints analíticos con filtros por fecha, sucursal, producto y cajero:
  * Resumen de ventas, ventas por sucursal, por producto y por cajero.
  * Comparativa entre períodos con cálculo de crecimiento porcentual.
  * Estado del inventario (total, bajo stock, sin stock, valor total).
  * Rendimiento de productos con tasa de rotación de inventario.
  * Reporte de cierres de caja con detección de discrepancias.
* 👤 **Gestión de Usuarios (Admin CRUD + Perfil propio):**
  * CRUD completo de usuarios con filtros y paginación (solo ADMIN).
  * Actualización de rol independiente (`PUT /users/{id}/role`).
  * Endpoints de perfil propio para cualquier usuario autenticado (`GET/PUT /profile`, `POST /profile/change-password`).
* 🛡️ **Auditoría:** Trazabilidad completa (**Quién, Cuándo, Qué**) en operaciones críticas.
* ⚡ **Integridad Transaccional:** Gestión estricta (`@Transactional`) en ventas, cancelaciones y movimientos de inventario.
* 🔍 **Especificaciones JPA:** Filtrado dinámico y paginación eficiente de catálogos.
* 🐳 **Containerización:** Entorno MySQL y Redis orquestado con **Docker Compose**.
* 🧪 **Testing:** Cobertura de integración y unitaria con **JUnit 5 y Mockito**, incluyendo Testcontainers con Redis real para rate limiting.

## 🛠️ Tech Stack

| Área | Tecnología | Propósito |
| --- | --- | --- |
| **Core** | Java 17, Spring Boot 3.4 | Lógica y Framework principal |
| **Persistencia** | Spring Data JPA / Hibernate | ORM y manejo de datos |
| **Base de Datos** | MySQL 8.0 (Prod) / H2 (Test) | Almacenamiento relacional |
| **Caché / NoSQL** | Redis | Rate Limiting y Blacklist de Tokens |
| **Seguridad** | Spring Security | RBAC (Role-Based Access Control) |
| **DevOps** | Docker Compose | Despliegue de infraestructura |
| **Docs** | OpenAPI (Swagger) | Documentación interactiva |
| **Testing** | JUnit 5, Mockito, Testcontainers | Tests unitarios e integración |

## 🚀 Guía de Despliegue (Local)

### Prerrequisitos

* Java JDK 17 o superior.
* Docker Desktop activo.

### 1. Iniciar Infraestructura

Levanta los contenedores de MySQL y Redis:

```bash
docker-compose up -d
```

### 2. Ejecutar Aplicación

Inicia el servidor Spring Boot (esto cargará datos de prueba automáticamente):

```bash
./mvnw spring-boot:run
```

No hace falta exportar ninguna variable de entorno para levantarlo así: `MYSQL_*`, `REDIS_*` y `JWT_EXPIRATION` tienen valores por defecto que coinciden con `docker-compose.yml`. `JWT_SECRET` sí tiene un default, pero solo pensado para desarrollo — nunca lo uses en producción.

## 🐳 Docker

También se puede construir y correr la imagen directamente:

```bash
docker build -t supermarket-back .
docker run -p 8080:8080 \
  -e MYSQL_HOST=host.docker.internal -e MYSQL_PORT=3307 \
  -e REDIS_HOST=host.docker.internal -e REDIS_PORT=6379 \
  -e JWT_SECRET=<un-secreto-propio> \
  supermarket-back
```

## ☁️ Despliegue en producción

Pensado para desplegarse en **Railway** (backend + MySQL + Redis administrados en un mismo proyecto) con el frontend en Vercel/Netlify. Variables de entorno relevantes:

| Variable | Descripción | Default (dev) |
| --- | --- | --- |
| `PORT` | Puerto del servidor | `8080` |
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `dev` |
| `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DATABASE` / `MYSQL_USERNAME` / `MYSQL_PASSWORD` | Conexión a MySQL | `localhost` / `3307` / `supermarketdb` / `root` / `123456` |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Conexión a Redis | `localhost` / `6379` / (vacío) |
| `JWT_SECRET` | Secreto para firmar los JWT | ⚠️ sin default seguro — **obligatorio en producción** |
| `JWT_EXPIRATION` | Expiración del token (ms) | `86400000` (24h) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos, separados por coma | `http://localhost:4200,http://localhost:3000` |

`data.sql` siembra la base automáticamente en el primer arranque (`spring.sql.init.mode=always`), así que una base nueva en producción queda lista con los usuarios de prueba sin pasos manuales.

## 🔄 Lógica de Negocio: El Ciclo de Venta

La clase `SaleServiceImpl` garantiza principios **ACID** y trazabilidad:

1. **Auditoría Automática:** Se captura al usuario autenticado del contexto de seguridad para vincularlo a la venta.
2. **Verificación de Caja Abierta:** Antes de procesar la venta, se valida que exista una caja registradora con estado `OPEN` en la sucursal. Si no la hay, la operación se rechaza.
3. **Bloqueo de Inventario (Lock):**
   * Verificación de stock en la sucursal específica.
   * **Fail-fast:** Si falta stock de un ítem, la transacción se aborta (`InsufficientStockException`).
4. **Cálculo Inmutable:** El backend calcula los precios basándose en la base de datos, ignorando valores externos.
5. **Persistencia Atómica:** Cabecera y detalles se guardan juntos. En caso de error, se hace rollback del stock descontado.

## 📡 Endpoints Principales

### 🔐 Auth & Seguridad (`/api/auth`)
* `POST /api/auth/login` - Obtención de Token JWT (con rate limiting: 5 intentos / 5 min). No hay auto-registro público: los usuarios los crea un ADMIN vía `POST /users`.
* `POST /api/auth/logout` - Invalida el token actual en Redis.

### 📍 Sucursales (`/branches`)
* `GET /branches` - Listado general (ADMIN, MANAGER).
* `POST /branches` - Alta de sucursal (ADMIN).

### 🛍️ Inventario (`/inventory`)
* `GET /inventory/low-stock` - **Alert System**: Detecta productos a reponer globalmente.
* `GET /inventory/branches/{branchId}/low-stock` - Bajo stock por sucursal.

### 📦 Productos (`/products`)
* `GET /products` - Búsqueda paginada con filtros (`name`, `category`, `price`).
* `GET /products/all` - Lista simple para dropdowns.

### 💰 Transacciones (`/sales`)
* `POST /sales` - Procesar nueva venta (requiere caja abierta en la sucursal).
* `POST /sales/{id}/cancel` - Anulación con motivo: revierte stock automáticamente (ADMIN/MANAGER).
* `GET /cashier/my-sales` - Historial paginado del cajero autenticado.

### 🏦 Caja Registradora (`/cash-registers`)
* `POST /cash-registers/open` - Apertura de turno con saldo inicial.
* `POST /cash-registers/{id}/close` - Cierre con saldo final.
* `GET /cash-registers/branches/{branchId}/current` - Caja activa de una sucursal.

### 💳 Pagos (`/payments`)
* `POST /payments` - Registrar pago para una venta (valida que no supere el total).
* `GET /payments/sale/{saleId}` - Pagos de una venta.

### 🔄 Transferencias de Stock (`/transfers`)
* `POST /transfers` - Solicitar transferencia entre sucursales.
* `POST /transfers/{id}/approve` - Aprobar solicitud pendiente (ADMIN/MANAGER).
* `POST /transfers/{id}/reject` - Rechazar con motivo (ADMIN/MANAGER).
* `POST /transfers/{id}/complete` - Ejecutar movimiento real de stock (ADMIN/MANAGER).
* `POST /transfers/{id}/cancel` - Cancelar (solicitante o ADMIN).
* `GET /transfers/status/{status}` - Filtrar por estado.

### 📊 Reportes (`/reports`) — ADMIN/MANAGER
* `GET /reports/sales/summary` - Resumen global de ventas con filtros.
* `GET /reports/sales/by-branch` - Ventas agrupadas por sucursal.
* `GET /reports/sales/by-product` - Ventas por producto con paginación.
* `GET /reports/sales/by-cashier` - Rendimiento por cajero con ticket promedio.
* `GET /reports/sales/comparison` - Comparativa del período actual vs. período anterior.
* `GET /reports/inventory/status` - Estado global del inventario.
* `GET /reports/inventory/performance` - Rendimiento de productos con tasa de rotación.
* `GET /reports/cash-registers` - Reporte de cierres con detección de discrepancias.

### 👤 Usuarios (`/users`) — ADMIN
* `GET /users` - Lista con filtros (username, email, rol) y paginación.
* `POST /users` - Crear usuario con cualquier rol.
* `PUT /users/{id}/role` - Cambiar rol de un usuario.
* `DELETE /users/{id}` - Desactivación lógica (soft delete).

### 🙋 Perfil propio (`/profile`)
* `GET /profile` - Ver perfil del usuario autenticado.
* `PUT /profile` - Actualizar username, nombre y apellido.
* `POST /profile/change-password` - Cambiar contraseña con validación de la actual.

## 📝 Ejemplo de Venta (Payload)

**Request (`POST /sales`):**
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

## 🔎 Accesos

| Recurso | URL |
| --- | --- |
| **Swagger UI** | `http://localhost:8080/swagger-ui/index.html` |
| **Docs JSON** | `http://localhost:8080/v3/api-docs` |
| **DB (MySQL)** | `jdbc:mysql://localhost:3307/supermarketdb` |

---

**Autor:** Guillermo — Java Backend Developer
