 
---

## 🛒 Supermarket Management System — Portfolio Junior

### ¿Qué es este proyecto?

API REST completa que simula la gestión de un supermercado real, desarrollada con **Spring Boot 3.4.1**.
El sistema maneja sucursales, productos y ventas, aplicando principios de **Clean Architecture** y **SOLID** para asegurar un código ordenado y escalable.

El objetivo es **demostrar buenas prácticas de desarrollo backend**: uso de DTOs, validaciones, manejo de excepciones, control de transacciones y testing.

---

## ✨ Características principales

✅ **CRUD completo** para sucursales, productos y ventas.
✅ **Control transaccional de stock** (no permite ventas si falta inventario).
✅ **Paginación y filtros** avanzados para el catálogo de productos.
✅ **Manejo global de errores** (respuestas JSON estandarizadas).
✅ **Validaciones automáticas** (Jakarta Validation) para datos seguros.
✅ **Documentación interactiva** con Swagger UI.
✅ **Base de datos H2** (memoria) para desarrollo rápido y **MySQL** para producción.

---

## 🔄 Flujo de una venta (explicado fácil)

Cuando se registra una venta, la API sigue estos pasos estrictos para evitar errores:

1. **Validación Inicial**
Verifica que la sucursal y los productos existan en la base de datos.
2. **Verificación de Stock**
Comprueba si hay suficiente cantidad de cada producto antes de procesar nada.
3. **Cálculo Automático**
El servidor calcula los subtotales y el total final (no confía en los datos del cliente).
4. **Transacción Segura**
Descuenta el stock y guarda la venta. Si algo falla aquí, **se revierte todo** para no dejar datos corruptos.
5. **Respuesta**
Devuelve la venta con estado `REGISTERED` y el total confirmado.

---

## 📌 Endpoints principales

### 📍 Sucursales (`/branches`)

* `GET /branches` — Listar todas las sucursales.
* `POST /branches` — Crear nueva sucursal.
* `DELETE /branches/{id}` — Eliminar sucursal (protegido si tiene datos asociados).

---

### 🛍️ Productos (`/products`)

* `GET /products` — Catálogo con **paginación y filtros** (nombre, precio, categoría).
* `GET /products/all` — Lista simple sin paginar (ideal para selectores/combos).
* `GET /products/{id}` — Ver detalle de un producto.
* `POST /products` — Crear producto (valida nombre único).
* `PUT /products/{id}` — Actualizar precio o stock.
* `DELETE /products/{id}` — Eliminar producto.

---

### 💰 Ventas (`/sales`) — **Funcionalidad Core**

* `POST /sales` — Registrar nueva venta (descuenta stock).
* `PUT /sales/{id}` — Modificar venta (recalcula y ajusta el stock automáticamente).
* `DELETE /sales/{id}` — Cancelar venta (**devuelve el stock** a los productos).

---

## 🧾 Ejemplo de venta (JSON)

```json
{
  "branchId": 1,
  "date": "2026-01-19",
  "details": [
    { "productId": 1, "stock": 5 },
    { "productId": 3, "stock": 2 }
  ]
}

```

La API responde con:

* Estado de la venta (`REGISTERED`)
* Total calculado automáticamente
* Stock actualizado en base de datos

---

## 🔎 Herramientas disponibles

### Swagger UI

Interfaz visual para probar la API sin escribir código.
`http://localhost:8080/swagger-ui/index.html`

### Consola H2

Acceso directo a la base de datos en memoria.
`http://localhost:8080/h2-console`

* **JDBC URL:** `jdbc:h2:mem:supermarketdb`

---

## 🛠️ Tecnologías

| Capa | Tecnologías |
| --- | --- |
| **Backend** | Spring Boot 3.4.1, Spring Data JPA |
| **Arquitectura** | Layered Architecture, DTOs, SOLID |
| **Validación** | Jakarta Bean Validation |
| **Base de datos** | H2 (Dev), MySQL (Prod) |
| **Documentación** | SpringDoc OpenAPI, Swagger UI |
| **Productividad** | Lombok, Maven Wrapper |
| **Testing** | JUnit 5, Mockito, MockMvc |
