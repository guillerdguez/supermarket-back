
---

# 🛒 Supermarket Management System API

> API REST empresarial para la gestión integral de inventarios, sucursales y procesos de venta automatizados.

Este proyecto implementa una solución de backend robusta utilizando **Spring Boot 3.4.1**, diseñada bajo los principios de **Clean Architecture** y **SOLID**. El sistema no solo gestiona datos, sino que garantiza la integridad del negocio mediante un motor de ventas con control de stock transaccional.

---

## 🏗️ Arquitectura y Diseño de Software

El sistema se basa en una arquitectura de **N-Capas**, asegurando un bajo acoplamiento y una alta cohesión:

* **Capa de Presentación (REST Controllers):** Gestión de contratos de entrada/salida y códigos de estado HTTP.
* **Capa de Servicio (Business Logic):** Implementación de reglas de negocio complejas (Validación de stock, cálculos de totales, estados de venta).
* **Capa de Persistencia (Repositories):** Abstracción de datos mediante Spring Data JPA.
* **Domain Model:** Entidades ricas y manejo de estados mediante Enums (`REGISTERED`, `CANCELLED`).
* **Data Transfer Objects (DTO):** Desacoplamiento total entre la base de datos y la respuesta JSON enviada al cliente.

---

## 🌟 Características Técnicas

* ✅ **Transaccionalidad ACID:** Las ventas garantizan que el stock se reduzca solo si toda la operación es exitosa.
* ✅ **Manejo de Errores Global:** Implementación de `@RestControllerAdvice` para respuestas estandarizadas.
* ✅ **Validación Declarativa:** Uso de `Jakarta Validation` para asegurar la integridad de los datos.
* ✅ **Documentación Viva:** Swagger UI integrado para pruebas automáticas.
* ✅ **Detección de Conflictos:** Gestión de duplicados y recursos no encontrados con excepciones personalizadas.

---

## 📂 Estructura del Proyecto

```plaintext
src/main/java/com/supermarket/supermarket/
├── controller/    # Endpoints REST (API Gateways)
├── service/       # Interfaces y lógica de negocio (S.O.L.I.D.)
│   └── impl/      # Implementaciones concretas
├── repository/    # Abstracción de base de datos (JPA)
├── model/         # Entidades de dominio y Enums
├── dto/           # Data Transfer Objects (Request/Response)
├── mapper/        # Transformadores de datos (manual mapping)
└── exception/     # Handler global y errores personalizados

```

---

## 📑 Documentación de la API

### 🔹 Sucursales (`/branches`)

* `GET /branches` - Listado completo de sucursales.
* `POST /branches` - Registro de nueva sucursal.
* `DELETE /branches/{id}` - Baja de sucursal (Protegida contra integridad referencial).

### 🔹 Productos (`/products`)

* `GET /products` - Consulta de catálogo y stock disponible.
* `PUT /products/{id}` - Actualización de precio, stock o categoría.

### 🔹 Ventas (`/sales`) - Operación Crítica

* `POST /sales` - Registro de transacción comercial.
* **Lógica Interna:** Busca producto ➔ Valida stock ➔ Calcula Subtotales ➔ Descuenta Stock ➔ Genera Venta.

**Cuerpo de petición (POST):**

```json
{
  "branchId": 1,
  "date": "2026-01-19",
  "details": [
    { "productId": 1, "quantity": 10 }
  ]
}

```

---

## 🚀 Instalación y Despliegue

1. **Clonación:**
```bash
git clone [https://github.com/guillerdguez/Supermarket.git](https://github.com/guillerdguez/Supermarket.git)

```


2. **Compilación y Tests:**
```bash
./mvnw clean install

```


3. **Ejecución:**
```bash
./mvnw spring-boot:run

```


4. **Swagger UI:**
Accede a: [http://localhost:8080/swagger-ui/index.html](https://www.google.com/search?q=http://localhost:8080/swagger-ui/index.html)

---

## 🧪 Calidad de Código (Testing)

Se ha implementado una suite de pruebas para asegurar la estabilidad del sistema:

* **Unit Tests:** Pruebas aisladas de lógica en servicios y mappers.
* **WebMvc Tests:** Validación de controladores y contratos JSON.
* **Mocking:** Uso exhaustivo de Mockito para simular la persistencia.

Para ejecutar el reporte de pruebas:

```bash
./mvnw test

```

---

## 🛠️ Tecnologías

* **Framework:** Spring Boot 3.4.1
* **Database:** H2 (Dev) / MySQL (Prod)
* **Documentation:** SpringDoc OpenAPI 2.7.0
* **Lombok:** Productividad y reducción de boilerplate.
* **Maven Wrapper:** Consistencia de entorno.

---

Desarrollado por **[Guillermo]** - 2026

---
