# API REST de Clientes — Spring Boot

> **Laboratorio N.º 04 — IS-388 Construcción y Evolución de Software**
> Sesión 04: Gestión Ágil Avanzada (Scrum en la construcción de software)

| Dato | Detalle |
|---|---|
| **Estudiante** | Henry Max Ayala Cuarez|
| **Código  | 27230118 |
| **Escuela Profesional** | Ingeniería de Sistemas — UNSCH, Ayacucho |
| **Docente** | Mtro. Ing. Oscar Meneses Yaranga |
---

## Tabla de contenido

1. [Visión del producto](#1-visión-del-producto)
2. [Requisitos técnicos de arquitectura](#2-requisitos-técnicos-de-arquitectura)
3. [Especificación del contrato de API](#3-especificación-del-contrato-de-api)
4. [Historias de usuario con criterios de aceptación (BDD)](#4-historias-de-usuario-con-criterios-de-aceptación-bdd)
5. [Lista de cotejo: Definition of Ready (DoR)](#5-lista-de-cotejo-definition-of-ready-dor)
6. [Lista de cotejo: Definition of Done (DoD)](#6-lista-de-cotejo-definition-of-done-dod)
7. [Refinamiento técnico y estimación](#7-refinamiento-técnico-y-estimación)
8. [Flujo de trabajo Git y Pull Requests](#8-flujo-de-trabajo-git-y-pull-requests)
9. [Cómo ejecutar el proyecto](#9-cómo-ejecutar-el-proyecto)
10. [Métricas ágiles a monitorear](#10-métricas-ágiles-a-monitorear)

---

## 1. Visión del producto

Se requiere un **microservicio REST** que permita a los equipos operativos y de análisis **registrar, consultar, actualizar y eliminar clientes**, además de **buscarlos rápidamente por DNI o por nombre**.

**Roles Scrum simulados en el laboratorio**

| Rol | Responsabilidad en este caso |
|---|---|
| Product Owner | Prioriza el Product Backlog y valida los criterios de aceptación. |
| Scrum Master | Facilita ceremonias y remueve impedimentos. |
| Developer Senior | Referente de arquitectura; lidera el refinamiento técnico y el code review. |
| Developers | Implementan, prueban y entregan el incremento. |

**Alcance (in scope)**: CRUD de clientes, búsqueda por DNI y nombre, validaciones, manejo global de errores, documentación OpenAPI y pruebas automatizadas.

**Fuera de alcance (out of scope)**: autenticación/autorización, persistencia en disco, paginación, despliegue productivo.

---

## 2. Requisitos técnicos de arquitectura

### 2.1 Stack tecnológico

| Aspecto | Requisito |
|---|---|
| **Lenguaje** | Java 21 o 25 |
| **Framework** | Spring Boot 3.x |
| **Módulos Spring** | Spring Web, Spring Data JPA, Validation (Jakarta Bean Validation) |
| **Persistencia temporal** | H2 en memoria: `jdbc:h2:mem:clientesdb` |
| **Consola de inspección** | H2 Console habilitada para revisar tablas en pruebas locales |
| **Manejo de errores** | `@RestControllerAdvice` global con respuestas `ProblemDetail` (RFC 7807) |
| **Documentación de API** | Swagger / OpenAPI (springdoc-openapi) |
| **Pruebas** | JUnit 5 + Mockito (unitarias) y `@SpringBootTest` (integración) |
| **Gestor de construcción** | Maven (o Gradle) |
| **CI/CD** | GitHub Actions: compilar, probar y analizar en cada Pull Request |
| **Calidad estática** | Codacy o SonarQube (vulnerabilidades, code smells y cobertura) |
| **Control de versiones** | Git + GitHub |
| **Tablero ágil** | GitHub Projects (o Jira) |

### 2.2 Arquitectura en capas

Se adopta una arquitectura en capas clásica. Cada capa depende únicamente de la inmediata inferior, lo que mantiene el código testeable y facilita la evolución (principios SOLID).

```
┌──────────────────────────────────────────────┐
│  Cliente HTTP (Postman / Swagger UI / front) │
└───────────────────────┬──────────────────────┘
                        │  JSON / HTTP
┌───────────────────────▼──────────────────────┐
│  Capa Web        ClienteController           │  ← Valida entrada (@Valid), mapea HTTP
│                  GlobalExceptionHandler      │  ← Errores → ProblemDetail (RFC 7807)
├──────────────────────────────────────────────┤
│  Capa Servicio   ClienteService              │  ← Reglas de negocio (DNI único)
├──────────────────────────────────────────────┤
│  Capa Datos      ClienteRepository (JPA)     │  ← Consultas derivadas
├──────────────────────────────────────────────┤
│  Persistencia    H2 en memoria (clientesdb)  │
└──────────────────────────────────────────────┘
```

**Decisiones de diseño y su porqué**

- **DTOs separados de la entidad**: el contrato público (`ClienteRequestDTO` / `ClienteResponseDTO`) no queda acoplado al esquema de base de datos; se puede cambiar uno sin romper el otro.
- **Regla de unicidad en dos niveles**: el servicio valida el DNI duplicado (para devolver un 409 claro) y la base de datos mantiene una restricción `UNIQUE` (protege ante condiciones de carrera).
- **Un solo punto de manejo de errores**: `@RestControllerAdvice` evita `try/catch` repetidos en los controladores y garantiza un formato de error uniforme.
- **H2 en memoria**: arranque inmediato y cero configuración externa; los datos se pierden al reiniciar, lo cual es aceptable para el laboratorio.

### 2.3 Estructura de paquetes propuesta

```
src/main/java/pe/edu/unsch/clientes
├── ClientesApplication.java
├── controller
│   └── ClienteController.java
├── service
│   ├── ClienteService.java
│   └── impl/ClienteServiceImpl.java
├── repository
│   └── ClienteRepository.java
├── model
│   └── Cliente.java                    # Entidad JPA
├── dto
│   ├── ClienteRequestDTO.java
│   └── ClienteResponseDTO.java
├── mapper
│   └── ClienteMapper.java
├── exception
│   ├── ClienteNotFoundException.java
│   ├── DniDuplicadoException.java
│   └── GlobalExceptionHandler.java
└── config
    └── OpenApiConfig.java

src/test/java/pe/edu/unsch/clientes
├── service/ClienteServiceTest.java          # JUnit 5 + Mockito
└── controller/ClienteControllerIT.java      # @SpringBootTest + MockMvc
```

### 2.4 Modelo de datos

**Entidad `Cliente`**

| Campo | Tipo | Restricciones |
|---|---|---|
| `id` | `Long` | PK, autogenerada (`IDENTITY`) |
| `dni` | `String` | Obligatorio, **exactamente 8 dígitos**, **único** |
| `nombre` | `String` | Obligatorio, no vacío |
| `email` | `String` | Obligatorio, formato de correo válido |

### 2.5 Reglas de validación

| Campo | Regla | Anotación sugerida |
|---|---|---|
| `dni` | Obligatorio, 8 dígitos numéricos | `@NotBlank` + `@Pattern(regexp = "\\d{8}")` |
| `nombre` | Obligatorio | `@NotBlank` |
| `email` | Obligatorio y con formato válido | `@NotBlank` + `@Email` |

### 2.6 Configuración base (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:clientesdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  h2:
    console:
      enabled: true
      path: /h2-console
  mvc:
    problemdetails:
      enabled: true

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

### 2.7 Dependencias principales (Maven)

```xml
<dependencies>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>runtime</scope></dependency>
    <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId><version>2.6.0</version></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
</dependencies>
```

> `spring-boot-starter-test` ya incluye JUnit 5 y Mockito. Verificar la versión vigente de `springdoc-openapi` al crear el proyecto.

### 2.8 Manejo global de excepciones

| Excepción | Código HTTP | Cuándo ocurre |
|---|---|---|
| `MethodArgumentNotValidException` | `400 Bad Request` | El payload incumple las validaciones |
| `ClienteNotFoundException` | `404 Not Found` | El `id` no existe |
| `DniDuplicadoException` | `409 Conflict` | Ya existe un cliente con ese DNI |
| `Exception` (genérica) | `500 Internal Server Error` | Error inesperado (sin exponer detalles internos) |

**Ejemplo de respuesta (`application/problem+json`)**

```json
{
  "type": "about:blank",
  "title": "Conflict",
  "status": 409,
  "detail": "Ya existe un cliente registrado con el DNI 12345678",
  "instance": "/api/v1/clientes"
}
```

### 2.9 Requisitos no funcionales

| Atributo | Requisito |
|---|---|
| **Mantenibilidad** | Principios SOLID, capas separadas, métodos cortos y nombres expresivos. |
| **Testabilidad** | Lógica de negocio aislada en el servicio para poder simularla con Mockito. |
| **Calidad** | Cobertura mínima acordada por el equipo (propuesta: ≥ 80 % en la capa de servicio). |
| **Seguridad** | Sin vulnerabilidades críticas en el análisis estático; no exponer trazas de error al cliente. |
| **Documentación** | OpenAPI siempre sincronizada con el código; README actualizado. |
| **Portabilidad** | Ejecutable con `mvn spring-boot:run`, sin instalar bases de datos externas. |

---

## 3. Especificación del contrato de API

**URL base:** `/api/v1/clientes`

| Método | Endpoint | Descripción | Parámetros / Body | Código HTTP esperado |
|---|---|---|---|---|
| `POST` | `/api/v1/clientes` | Crear cliente | Body: `ClienteRequestDTO` | `201 Created` / `400 Bad Request` / `409 Conflict` |
| `GET` | `/api/v1/clientes` | Listar / Buscar | Query params opcionales: `dni`, `nombre` | `200 OK` |
| `GET` | `/api/v1/clientes/{id}` | Obtener por ID | Path param: `id` | `200 OK` / `404 Not Found` |
| `PUT` | `/api/v1/clientes/{id}` | Actualizar cliente | Path param: `id`, Body: `ClienteRequestDTO` | `200 OK` / `400 Bad Request` / `404 Not Found` / `409 Conflict` |
| `DELETE` | `/api/v1/clientes/{id}` | Eliminar cliente | Path param: `id` | `204 No Content` / `404 Not Found` |

**`ClienteRequestDTO`**

```json
{
  "dni": "12345678",
  "nombre": "Carlos Quispe",
  "email": "carlos.quispe@example.com"
}
```

**`ClienteResponseDTO`**

```json
{
  "id": 1,
  "dni": "12345678",
  "nombre": "Carlos Quispe",
  "email": "carlos.quispe@example.com"
}
```

**Comportamiento de la búsqueda (`GET /api/v1/clientes`)**

- Sin parámetros → devuelve todos los clientes.
- `?dni=12345678` → coincidencia **exacta**.
- `?nombre=carlos` → coincidencia **parcial**, **sin distinguir mayúsculas/minúsculas**.
- Si se envían ambos, se aplican los dos filtros (decisión de diseño, a confirmar con el Product Owner en el refinamiento).

---

## 4. Historias de usuario con criterios de aceptación (BDD)

### HU-01: Gestión CRUD de Clientes

> **Como** Analista de Datos,
> **quiero** registrar, consultar, actualizar y eliminar clientes,
> **para** mantener actualizada la base de datos operativa.

**Criterios de aceptación (Given / When / Then)**

**Escenario 1 — Creación exitosa**
- **Given** que envío una solicitud `POST /api/v1/clientes` con un payload válido (DNI único, nombre, email)
- **When** la API procesa el registro
- **Then** responde con `HTTP 201 Created`, incluye el header `Location` y el cliente generado con su ID

**Escenario 2 — Validación de DNI duplicado**
- **Given** que ya existe un cliente registrado con el DNI `"12345678"`
- **When** intento registrar otro cliente con el mismo DNI `"12345678"`
- **Then** la API responde con `HTTP 409 Conflict` indicando el fallo de unicidad

**Escenarios complementarios** (cubren el resto del contrato de la sección 3)

**Escenario 3 — Datos inválidos**
- **Given** que envío un `POST` con un DNI de menos de 8 dígitos o un email mal formado
- **When** la API valida el payload
- **Then** responde con `HTTP 400 Bad Request` y un `ProblemDetail` que identifica los campos inválidos

**Escenario 4 — Consulta por ID existente**
- **Given** que existe un cliente con `id = 1`
- **When** hago `GET /api/v1/clientes/1`
- **Then** responde con `HTTP 200 OK` y los datos del cliente

**Escenario 5 — Consulta por ID inexistente**
- **Given** que no existe un cliente con `id = 999`
- **When** hago `GET /api/v1/clientes/999`
- **Then** responde con `HTTP 404 Not Found` en formato `ProblemDetail`

**Escenario 6 — Actualización exitosa**
- **Given** que existe un cliente con `id = 1`
- **When** envío `PUT /api/v1/clientes/1` con datos válidos
- **Then** responde con `HTTP 200 OK` y el cliente actualizado

**Escenario 7 — Actualización con DNI de otro cliente**
- **Given** que el DNI `"87654321"` pertenece a otro cliente distinto al del `id` indicado
- **When** intento actualizar con ese DNI
- **Then** responde con `HTTP 409 Conflict`

**Escenario 8 — Eliminación exitosa**
- **Given** que existe un cliente con `id = 1`
- **When** hago `DELETE /api/v1/clientes/1`
- **Then** responde con `HTTP 204 No Content` y el cliente deja de existir

**Escenario 9 — Eliminación de cliente inexistente**
- **Given** que no existe un cliente con `id = 999`
- **When** hago `DELETE /api/v1/clientes/999`
- **Then** responde con `HTTP 404 Not Found`

---

### HU-02: Búsqueda y filtrado por DNI o Nombre

> **Como** Operador del Sistema,
> **quiero** realizar consultas por DNI o por coincidencia de nombre,
> **para** ubicar rápidamente la información del cliente.

**Criterios de aceptación (Given / When / Then)**

**Escenario 1 — Búsqueda exacta por DNI**
- **Given** que realizo una petición `GET /api/v1/clientes?dni=12345678`
- **When** la API ejecuta la consulta en H2
- **Then** retorna `HTTP 200 OK` con los datos del cliente encontrado

**Escenario 2 — Búsqueda parcial por nombre**
- **Given** que realizo una petición `GET /api/v1/clientes?nombre=Carlos`
- **When** la API busca coincidencias parciales sin diferenciar mayúsculas/minúsculas (case-insensitive)
- **Then** retorna `HTTP 200 OK` con el listado de clientes coincidentes

**Escenarios complementarios**

**Escenario 3 — Sin resultados**
- **Given** que ningún cliente tiene el DNI `"00000000"`
- **When** hago `GET /api/v1/clientes?dni=00000000`
- **Then** retorna `HTTP 200 OK` con una lista vacía `[]`

**Escenario 4 — Insensibilidad a mayúsculas**
- **Given** que existe el cliente "Carlos Quispe"
- **When** busco con `?nombre=cArLoS`
- **Then** el cliente aparece en el listado devuelto

---

## 5. Lista de cotejo: Definition of Ready (DoR)

La **DoR** es el acuerdo entre el Product Owner y los desarrolladores para asegurar que una historia está comprendida, libre de bloqueos y lista para entrar al Sprint. **Se marca antes de mover una tarjeta a *To Do* o *In Progress*.**

### 5.1 DoR general (técnico)

| Categoría | Criterio de cotejo | Verificación |
|---|---|:---:|
| Marco INVEST | La historia es independiente, negociable, aporta valor, es estimable, pequeña y testeable. | `[ ]` |
| Criterios de aceptación | Están redactados de forma explícita y verificable (formato BDD: Given / When / Then). | `[ ]` |
| Contratos de API e interfaces | La arquitectura, los esquemas de base de datos y los contratos de integración están prediseñados y acordados. | `[ ]` |
| Gestión de dependencias | Las dependencias de infraestructura, servicios externos o tareas previas han sido resueltas. | `[ ]` |
| Estimación técnica | La historia cuenta con estimación en Story Points (Planning Poker / Fibonacci) asignada por el equipo. | `[ ]` |

### 5.2 DoR específica para esta tarea

- [ ] La historia de usuario cumple el principio **INVEST** (es independiente, pequeña y testeable).
- [ ] El **contrato de API** (DTOs, rutas y respuestas HTTP) está prediseñado y acordado.
- [ ] Las **reglas de validación** (DNI obligatorio de 8 dígitos, formato de email) están documentadas.
- [ ] La **estimación en Story Points** fue asignada por el equipo técnico.

### 5.3 Verificación INVEST aplicada a las historias del caso

| Criterio | HU-01 (CRUD) | HU-02 (Búsqueda) |
|---|---|---|
| **I**ndependent | Sí: no depende de otra historia. | Parcial: necesita que existan clientes (se resuelve con datos de prueba o tras HU-01). |
| **N**egotiable | Sí: el alcance de campos y escenarios es discutible con el PO. | Sí: el comportamiento al combinar filtros es negociable. |
| **V**aluable | Sí: mantiene la base operativa actualizada. | Sí: reduce el tiempo de ubicar un cliente. |
| **E**stimable | Sí: contrato y reglas definidos. | Sí. |
| **S**mall | Debe dividirse en tareas técnicas (ver sección 7). | Sí: cabe en un sprint con holgura. |
| **T**estable | Sí: 9 escenarios BDD. | Sí: 4 escenarios BDD. |

---

## 6. Lista de cotejo: Definition of Done (DoD)

La **DoD** es el compromiso formal que garantiza que el incremento cumple los estándares de calidad de producción. **Se usa como puerta de calidad durante el code review antes de autorizar el *merge* a la rama principal.**

### 6.1 DoD general (técnico)

| Categoría | Criterio de cotejo | Verificación |
|---|---|:---:|
| Revisión de código (PR) | El código fue revisado y aprobado vía Pull Request por al menos un Developer Senior o par. | `[ ]` |
| Tamaño atómico del PR | El cambio es conciso y enfocado (idealmente entre 200 y 400 líneas). | `[ ]` |
| Pruebas y CI/CD | La suite de pruebas unitarias e integración se ejecuta y pasa en el pipeline automatizado. | `[ ]` |
| Análisis estático | Codacy o SonarQube confirman ausencia de vulnerabilidades de seguridad y code smells graves. | `[ ]` |
| Documentación técnica | La documentación de la API, los contratos y los README fueron actualizados. | `[ ]` |
| Despliegue | El código está desplegado y verificado en el entorno de Staging/QA. | `[ ]` |

### 6.2 DoD específica para esta tarea

- [ ] **Código funcional** con la base de datos **H2 en memoria** configurada.
- [ ] **Pruebas unitarias** (JUnit 5 + Mockito) y **de integración** (`@SpringBootTest`) ejecutadas con éxito en el pipeline de CI/CD.
- [ ] **Pull Request atómico** (< 300 líneas) revisado y aprobado por un desarrollador senior.
- [ ] **Análisis estático** libre de vulnerabilidades y con **cobertura de código mínima** aprobada.
- [ ] **Documentación interactiva** habilitada en Swagger / OpenAPI.

### 6.3 Trazabilidad: criterio de aceptación → prueba

| Escenario | Tipo de prueba | Clase sugerida |
|---|---|---|
| HU-01 · E1 Creación exitosa | Integración | `ClienteControllerIT` |
| HU-01 · E2 DNI duplicado | Unitaria + Integración | `ClienteServiceTest`, `ClienteControllerIT` |
| HU-01 · E3 Datos inválidos | Integración | `ClienteControllerIT` |
| HU-01 · E4–E5 Consulta por ID | Unitaria + Integración | `ClienteServiceTest`, `ClienteControllerIT` |
| HU-01 · E6–E7 Actualización | Unitaria + Integración | `ClienteServiceTest`, `ClienteControllerIT` |
| HU-01 · E8–E9 Eliminación | Unitaria + Integración | `ClienteServiceTest`, `ClienteControllerIT` |
| HU-02 · E1–E4 Búsquedas | Integración | `ClienteControllerIT` |

---

## 7. Refinamiento técnico y estimación

Desglose de las historias en **tareas técnicas operativas de corta duración (< 1–2 días)**, según lo pedido en el Módulo 2 de la guía.

### 7.1 Tareas técnicas

| ID | Tarea | Historia |
|---|---|---|
| T-01 | Inicializar proyecto Spring Boot (Web, JPA, Validation, H2, springdoc) y configurar `application.yml` | HU-01 |
| T-02 | Crear entidad `Cliente` y `ClienteRepository` | HU-01 |
| T-03 | Crear `ClienteRequestDTO`, `ClienteResponseDTO` y `ClienteMapper` con validaciones | HU-01 |
| T-04 | Implementar `ClienteService` (crear, obtener, actualizar, eliminar) con regla de DNI único | HU-01 |
| T-05 | Implementar `ClienteController` con los 5 endpoints | HU-01 |
| T-06 | Implementar `GlobalExceptionHandler` con `ProblemDetail` | HU-01 |
| T-07 | Pruebas unitarias del servicio (JUnit 5 + Mockito) | HU-01 |
| T-08 | Consultas derivadas por DNI y nombre (`findByDni`, `findByNombreContainingIgnoreCase`) | HU-02 |
| T-09 | Lógica de filtrado en servicio y controlador (`?dni`, `?nombre`) | HU-02 |
| T-10 | Pruebas de integración de CRUD y búsqueda (`@SpringBootTest`) | HU-01 / HU-02 |
| T-11 | Configurar OpenAPI/Swagger y anotar endpoints | HU-01 / HU-02 |
| T-12 | Configurar pipeline de CI (build + tests + análisis estático) | Transversal |
| T-13 | Actualizar README y documentación técnica | Transversal |

### 7.2 Estimación inicial (Planning Poker, serie de Fibonacci)

> Son estimaciones **propuestas** para este trabajo individual; en un equipo real se obtienen por votación y debate hasta alinear discrepancias.

| Historia | Story Points | Justificación (esfuerzo / riesgo / complejidad) |
|---|:---:|---|
| HU-01 Gestión CRUD de Clientes | **5** | Cinco endpoints, validaciones, manejo global de errores y regla de unicidad; riesgo medio. |
| HU-02 Búsqueda y filtrado | **3** | Reutiliza la base de HU-01; consultas derivadas simples; riesgo bajo. |
| **Total del sprint** | **8** | |

---

## 8. Flujo de trabajo Git y Pull Requests

**Estrategia de ramas**

```
main                  ← siempre estable y desplegable
 └── develop          ← integración
      ├── feature/hu-01-crud-clientes
      └── feature/hu-02-busqueda-clientes
```

**Convención de commits** (Conventional Commits)

```
feat: agrega endpoint POST /api/v1/clientes
fix: corrige validación de DNI de 8 dígitos
test: agrega pruebas de integración para DNI duplicado
docs: actualiza README con la DoD
```

**Plantilla de Pull Request** (`.github/pull_request_template.md`)

```markdown
## Resumen de cambios
<!-- Qué se hizo y por qué -->

## Historia / tarea relacionada
Closes #<número>

## Plan de pruebas
- [ ] Pruebas unitarias ejecutadas
- [ ] Pruebas de integración ejecutadas
- [ ] Probado manualmente en Swagger UI

## Checklist de la DoD
- [ ] PR atómico (< 300 líneas)
- [ ] Pipeline de CI en verde
- [ ] Análisis estático sin vulnerabilidades
- [ ] Documentación/OpenAPI actualizada
- [ ] Revisado y aprobado por un desarrollador senior o par
```

**Clasificación de comentarios en code review**

| Etiqueta | Significado | Ejemplos |
|---|---|---|
| 🔴 **Bloqueante** | Debe corregirse antes del merge (viola la DoD o la seguridad). | Error de lógica, vulnerabilidad, fallo de arquitectura. |
| 🟡 **Sugerencia (nitpick)** | Opcional; mejora estética o de estilo. | Nombre de variable, orden de imports. |

> Los comentarios se enfocan **en el código y no en la persona**, con comunicación asertiva y empática.

---

## 9. Cómo ejecutar el proyecto

**Requisitos previos:** JDK 21+ y Maven 3.9+.

```bash
# 1. Clonar el repositorio
git clone https://github.com/<usuario>/<repositorio>.git
cd <repositorio>

# 2. Ejecutar pruebas
mvn clean test

# 3. Levantar la aplicación
mvn spring-boot:run
```

| Recurso | URL |
|---|---|
| API | `http://localhost:8080/api/v1/clientes` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Consola H2 | `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:clientesdb`, usuario `sa`, sin contraseña) |

**Ejemplos con `curl`**

```bash
# Crear cliente
curl -i -X POST http://localhost:8080/api/v1/clientes \
  -H "Content-Type: application/json" \
  -d '{"dni":"12345678","nombre":"Carlos Quispe","email":"carlos@example.com"}'

# Buscar por DNI
curl "http://localhost:8080/api/v1/clientes?dni=12345678"

# Buscar por nombre (parcial, case-insensitive)
curl "http://localhost:8080/api/v1/clientes?nombre=carlos"

# Eliminar
curl -i -X DELETE http://localhost:8080/api/v1/clientes/1
```

---

## 10. Métricas ágiles a monitorear

| Métrica | Qué mide | Uso en el sprint simulado |
|---|---|---|
| **Velocity** | Story Points completados por sprint | Referencia para planificar el siguiente sprint. |
| **Lead Time** | Tiempo desde que se pide la historia hasta que se entrega | Evalúa la rapidez de extremo a extremo. |
| **Cycle Time** | Tiempo desde *In Progress* hasta *Done* | Detecta ineficiencias en la ejecución. |
| **PR Review Lead Time** | Tiempo de espera en revisión de un PR | Detecta cuellos de botella en las entregas. |
| **Cobertura de código** | % de código ejercitado por pruebas | Respalda el criterio de la DoD. |

**Retrospectiva técnica (preguntas guía)**

1. ¿Se cumplieron los compromisos del sprint?
2. ¿Fue efectivo el refinamiento y la DoR?
3. ¿Qué mejoras hay en la comunicación durante las revisiones de código?

---

## Referencias

- Guía de prácticas de laboratorio N.º 04, IS-388 — UNSCH (2026-II).
- Fowler, M. (2015). *Yagni*. martinfowler.com.
- Martin, R. C. (2008). *Clean Code: A Handbook of Agile Software Craftsmanship*. Pearson.
- Khorikov, V. (2020). *Unit Testing Principles, Practices, and Patterns*. Manning.
- RFC 7807 — *Problem Details for HTTP APIs*.

---

*Documento elaborado de forma individual para el Laboratorio N.º 04 de IS-388 — Escuela Profesional de Ingeniería de Sistemas, UNSCH.*
