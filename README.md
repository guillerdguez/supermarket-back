 
---

# 🛒 Supermarket Management System (SMS) API

**Enterprise-Grade REST API** diseñada para la gestión integral de inventarios distribuidos, control de caja y ventas seguras.

## 💡 Sobre el Proyecto

Este proyecto es el núcleo backend de un sistema ERP para retail. Ha sido diseñado siguiendo principios de **Clean Architecture y SOLID**, priorizando la integridad financiera, la trazabilidad de operaciones y la seguridad.

El sistema resuelve el problema de la gestión de stock en múltiples sucursales y asegura que cada transacción sea atómica y auditable.

## 🗺️ Roadmap Técnico (Próximas Features)

El desarrollo actual se centra en consolidar la lógica de negocio del servidor:

* **Fase 2: Gestión de Caja (Cash Management):** Apertura y cierre de turnos con arqueo de caja y detección de diferencias.
* **Fase 3: Logística Interna:** Transferencias de stock entre sucursales con estados de aprobación.
* **Fase 4: Reportes Operativos:** Endpoints de inteligencia de negocio para análisis de ventas y rendimiento de cajeros.

## ✨ Características Técnicas Implementadas

Este backend implementa lógica de negocio compleja más allá de un simple CRUD:

* 🏗️ **Arquitectura Robusta:** Diseño modular en capas (`Controller`, `Service`, `Repository`, `Domain`).
* 🔐 **Seguridad Avanzada:**
* Autenticación vía **JWT** (JSON Web Tokens).
* **Rate Limiting** con Redis para prevenir fuerza bruta.
* **Token Blacklist** para invalidación real de sesiones al hacer Logout.


* ⚡ **Integridad Transaccional:** Gestión estricta (`@Transactional`) en ventas y movimientos de inventario.
* 🛡️ **Auditoría:** Trazabilidad completa (**Quién, Cuándo, Qué**) en operaciones críticas.
* 🔍 **Especificaciones JPA:** Filtrado dinámico y paginación eficiente de catálogos.
* 🐳 **Containerización:** Entorno MySQL y Redis orquestado con **Docker Compose**.
* 🧪 **Testing:** Cobertura de integración y unitaria con **JUnit 5 y Mockito**.

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

## 🔄 Lógica de Negocio: El Ciclo de Venta

La clase `SaleServiceImpl` garantiza principios **ACID** y trazabilidad:

1. **Auditoría Automática:** Se captura al usuario autenticado del contexto de seguridad para vincularlo a la venta (*Author*).
2. **Bloqueo de Inventario (Lock):**
* Verificación de stock en la sucursal específica.
* **Fail-fast:** Si falta stock de un ítem, la transacción se aborta (`InsufficientStockException`).


3. **Cálculo Inmutable:** El backend calcula los precios basándose en la base de datos, ignorando valores externos.
4. **Persistencia Atómica:** Cabecera y detalles se guardan juntos. En caso de error, se hace rollback del stock descontado.

## 📡 Endpoints Principales

### 📍 Sucursales (`/branches`)

* `POST /branches` - Alta de sucursal (Validación de unicidad).
* `GET /branches` - Listado general.

### 🛍️ Inventario (`/products`)

* `GET /products` - Búsqueda paginada con filtros (`name`, `category`, `price`).
* `GET /products/low-stock` - **Alert System**: Detecta productos a reponer en cada sucursal.

### 💰 Transacciones (`/sales`)

* `POST /sales` - Procesar nueva venta (Requiere rol **CASHIER** o superior).
* `POST /sales/{id}/cancel` - **Anulación**: Revierte la venta y restaura el stock automáticamente (Solo **ADMIN/MANAGER**).
* *(Nota: Las ventas son inmutables, no se permiten ediciones PUT, solo cancelaciones).*

### 🔐 Auth & Auditoría

* `POST /api/auth/login` - Obtención de Token JWT.
* `POST /api/auth/logout` - Invalida el token actual en Redis.

## 📝 Ejemplo de Venta (Payload)

**Request (`POST /sales`):**

```json
{
  "branchId": 1,
  "date": "2026-02-18",
  "details": [
    {
      "productId": 10,
      "stock": 2
    },
    {
      "productId": 5,
      "stock": 1
    }
  ]
}

```

**Respuesta (201 Created):**

```json
{
    "id": 125,
    "total": 3500.00,
    "status": "REGISTERED",
    "cashierName": "juan.perez",
    "createdAt": "2026-02-18 10:30:00",
    "details": [
        {
            "productId": 10,
            "quantity": 2,
            "subtotal": 2000.00
        },
        {
            "productId": 5,
            "quantity": 1,
            "subtotal": 1500.00
        }
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

**Autor:** Guillermo - Java Backend Developer
