 ---

## 🛒 Supermarket Management System — Portfolio Junior

### ¿Qué es este proyecto?

API REST completa que simula la gestión de un supermercado real, desarrollada con **Spring Boot 3.4.1** y **Java 17**.
El sistema maneja sucursales, productos y ventas, aplicando principios de **Clean Architecture** y **SOLID** para asegurar un código ordenado y escalable.

El entorno de desarrollo está contenerizado mediante **Docker**, utilizando MySQL para la persistencia de datos y H2 para entornos de prueba.

### ✨ Características principales

✅ **CRUD completo** para sucursales, productos y ventas.
✅ **Infraestructura Dockerizada** (MySQL 8.0 vía Docker Compose).
✅ **Control transaccional de stock** (Atomicidad en ventas masivas).
✅ **Paginación y filtros** avanzados (Criteria API / Specifications).
✅ **Sistema de Alertas:** Endpoint dedicado para productos con bajo stock.
✅ **Manejo global de errores** (`@RestControllerAdvice` y respuestas JSON estandarizadas).
✅ **Validaciones robustas** (Jakarta Validation) en DTOs.
✅ **Testing Unitario:** Cobertura con JUnit 5 y Mockito.
✅ **Documentación interactiva** con Swagger UI.

---

## 🚀 Guía de Inicio Rápido (Local)

El proyecto requiere **Docker** para la base de datos y **Java 17**.

### 1. Levantar infraestructura (Base de Datos)

Ejecuta el siguiente comando en la raíz del proyecto para iniciar MySQL en el puerto `3307`:

```bash
docker-compose up -d

```

### 2. Ejecutar la aplicación

Una vez la base de datos esté lista, inicia la aplicación Spring Boot:

```bash
./mvnw spring-boot:run

```

*La aplicación cargará automáticamente datos de prueba (`data.sql`) al iniciar.*

---

## 🔄 Flujo de una venta (Lógica de Negocio)

La clase `SaleServiceImpl` implementa un flujo transaccional estricto:

1. **Validación de Existencia:** Verifica sucursal y productos.
2. **Bloqueo y Reducción de Stock:**
* Agrupa cantidades por producto.
* Verifica disponibilidad en tiempo real.
* Lanza `InsufficientStockException` si falta inventario.


3. **Cálculo de Totales:** El backend calcula subtotales y total (ignora precios enviados por cliente).
4. **Persistencia Transaccional:** Si falla el guardado de algún detalle, se hace **rollback** del stock descontado.

---

## 📌 Endpoints principales

### 📍 Sucursales (`/branches`)

* `GET /branches`: Listar todas.
* `POST /branches`: Crear (valida nombres únicos).
* `DELETE /branches/{id}`: Borrado lógico/físico (protegido si tiene ventas).

### 🛍️ Productos (`/products`)

* `GET /products`: Catálogo paginado. Filtros disponibles:
* `name`: Búsqueda parcial.
* `category`: Filtrado exacto.
* `minPrice` / `maxPrice`: Rango de precios.


* `GET /products/all`: Lista completa (para dropdowns).
* `GET /products/low-stock`: **[Nuevo]** Alerta de stock bajo (param `amount` opcional, default 10).
* `POST /products`: Alta de producto.

### 💰 Ventas (`/sales`)

* `POST /sales`: Registrar venta (Transaction Script).
* `PUT /sales/{id}`: Modificar venta (Gestiona devolución y recálculo de stock).
* `DELETE /sales/{id}`: Cancelar venta (Restaura el stock automáticamente).

---

## 🧾 Ejemplo de venta (JSON)

```json
{
  "branchId": 1,
  "date": "2026-02-05",
  "details": [
    { "productId": 1, "stock": 5 },
    { "productId": 3, "stock": 2 }
  ]
}

```

**Respuesta exitosa:** Status `201 Created` con desglose de subtotales.

---

## 🔎 Herramientas y Accesos

| Herramienta | URL / Credenciales |
| --- | --- |
| **Swagger UI** | `http://localhost:8080/swagger-ui/index.html` |
| **API Docs (JSON)** | `http://localhost:8080/v3/api-docs` |
| **MySQL (Docker)** | `jdbc:mysql://localhost:3307/supermarketdb` |
| **Credenciales DB** | User: `root` / Pass: `123456` |

---

## 🛠️ Stack Tecnológico

| Capa | Tecnologías |
| --- | --- |
| **Backend** | Java 17, Spring Boot 3.4.1 |
| **Datos** | Spring Data JPA, Hibernate, MySQL 8.0 (Docker) |
| **Validación** | Jakarta Bean Validation |
| **Testing** | JUnit 5, Mockito, Spring Boot Test |
| **API Doc** | SpringDoc OpenAPI (Swagger) |
| **Herramientas** | Maven Wrapper, Lombok, Docker Compose |
