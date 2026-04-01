# Auth Backend - Spring Boot

## 📋 Descripción

Backend de autenticación JWT desarrollado con **Spring Boot 2.7.18** y **Java 1.8** para la prueba técnica de Econocom. Proporciona servicios RESTful de autenticación tradicional (email/password) y autenticación SSO simulada.

&gt; **Nota importante sobre SSO**: Se optó por devolver la URL de redirección con código **200 OK** en lugar de **302 Redirect**, debido a que los navegadores modernos siguen automáticamente las redirecciones 302 en peticiones AJAX, lo que impedía gestionar el flujo desde Angular. El frontend recibe la URL en el body JSON y realiza el proceso necesario.

---

## 🛠️ Tecnologías y Versiones

| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| Spring Boot | 2.7.18 | Framework principal (última versión con soporte Java 8) |
| Java | 1.8 | Versión requerida por especificaciones del proyecto |
| Spring Security | 5.7.x | Seguridad y autenticación JWT |
| Spring Data JPA | 2.7.x | Persistencia de datos |
| H2 Database | 2.1.x | Base de datos en memoria para desarrollo/pruebas |
| JJWT | 0.11.5 | Generación y validación de tokens JWT |
| JUnit | 4.13.2 | Framework de pruebas unitarias (estable para Java 8) |
| Mockito | 4.x | Mocking para pruebas |
| JaCoCo | 0.8.8 | Cobertura de código |
| Lombok | 1.18.x | Reducción de boilerplate |
| Apache Commons Lang3 | 3.12.0 | Utilidades de texto |

---

## 📁 Estructura del Proyecto

```
auth-backend/
├── src/
│   ├── main/
│   │   ├── java/com/econocom/auth/
│   │   │   ├── AuthApplication.java              # Punto de entrada
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java               # Configuración CORS
│   │   │   │   └── SecurityConfig.java           # Configuración Spring Security
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java           # Endpoints REST
│   │   │   ├── exception/
│   │   │   │   ├── AuthenticationException.java  # Excepción base
│   │   │   │   ├── GlobalExceptionHandler.java   # Manejo global de errores
│   │   │   │   ├── InvalidCredentialsException.java
│   │   │   │   └── SsoException.java
│   │   │   ├── filter/
│   │   │   │   └── JwtAuthenticationFilter.java  # Filtro JWT
│   │   │   ├── model/
│   │   │   │   ├── ErrorResponse.java            # Respuesta de error
│   │   │   │   ├── LoginRequest.java             # DTO Login
│   │   │   │   ├── LoginResponse.java            # DTO Respuesta
│   │   │   │   ├── SsoCallbackRequest.java
│   │   │   │   ├── SsoRedirectResponse.java
│   │   │   │   └── Usuario.java                  # Entidad JPA
│   │   │   ├── repository/
│   │   │   │   └── UsuarioRepository.java        # Repositorio JPA
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java              # Interfaz
│   │   │   │   └── AuthServiceImpl.java          # Implementación
│   │   │   └── util/
│   │   │       ├── JwtUtil.java                  # Utilidad JWT
│   │   │       └── LoggingUtil.java              # Logging estandarizado
│   │   └── resources/
│   │       ├── application.properties            # Configuración
│   │       └── data.sql                          # Datos iniciales
│   └── test/                                      # Pruebas unitarias e integración
│       └── java/com/econocom/auth/
│           ├── AuthFlowEndToEndTest.java
│           ├── config/SecurityConfigIntegrationTest.java
│           ├── controller/AuthControllerIntegrationTest.java
│           ├── exception/*Test.java
│           ├── filter/JwtAuthenticationFilterIntegrationTest.java
│           ├── repository/UsuarioRepositoryIntegrationTest.java
│           ├── service/AuthServiceImplTest.java
│           └── util/*Test.java
└── pom.xml
```

---

## 🚀 Endpoints API

### Autenticación Tradicional

| Método | Endpoint | Descripción | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/auth/login` | Login con email/password | No |
| `POST` | `/api/auth/login` | Body: `{"email":"...","password":"..."}` | No |

**Respuesta éxito (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600000,
  "tokenType": "Bearer"
}
```

**Respuesta error (401):**
```json
{
  "status": 401,
  "message": "Invalid credentials",
  "timestamp": 1234567890
}
```

### Autenticación SSO (Simulada)

| Método | Endpoint | Descripción | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/api/auth/sso` | Inicia flujo SSO, devuelve URL | No |
| `GET` | `/api/auth/sso/callback` | Callback con `code` y `state` | No |

**Inicio SSO (200):**
```json
{
  "ssoUrl": "http://localhost:8080/api/auth/sso/callback?code=simulated-sso-code-xxx&state=uuid"
}
```

**Callback éxito (200):** Mismo formato que login tradicional

**Callback error (400/401):**
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid SSO authorization code"
}
```

---

## ⚙️ Configuración

### Base de Datos H2 (Desarrollo/Tests)

```properties
spring.datasource.url=jdbc:h2:mem:authdb
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### JWT

```properties
jwt.secret=your256bitsigningkeyforjwttokenvalidationmustbe32bytesormore
jwt.expiration=3600000        # 1 hora
jwt.refresh-expiration=86400000  # 24 horas
```

### CORS

```properties
cors.allowed-origins=http://localhost:4200
```

---

## 🧪 Pruebas

### Ejecución de Tests

```bash
# Todos los tests
mvn test

# Solo tests unitarios
mvn test -Dtest="*Test" -DexcludedGroups="integration"

# Solo tests de integración
mvn test -Dtest="*IntegrationTest,*EndToEndTest"

# Cobertura de código (JaCoCo)
mvn clean test jacoco:report
# Reporte en: target/site/jacoco/index.html
```

### Estrategia de Pruebas

| Tipo | Framework | Cobertura |
|------|-----------|-----------|
| Unitarias | JUnit 4 + Mockito | Servicios, Utilidades, Excepciones |
| Integración | Spring Boot Test + MockMvc | Controllers, Filtros, Repositorios, Configuración |
| End-to-End | Spring Boot Test (WebEnvironment) | Flujos completos |

&gt; **Nota**: Se optó por **JUnit 4** en lugar de JUnit 5 debido a mayor madurez y estabilidad con **Spring Boot 2.7.x** y **Java 1.8**, minimizando riesgos de incompatibilidad.

---

## 🏗️ Arquitectura

### Diagrama de Flujo de Autenticación

```
┌─────────────┐     POST /api/auth/login     ┌──────────────┐
│   Cliente   │ ────────────────────────────→│  AuthController│
│  (Angular)  │                              │              │
└─────────────┘                              └──────┬───────┘
       │                                            │
       │←───────────────────────────────────────────┘
       │         200 OK + JWT Tokens
       │
       │     Autenticación SSO (Simulada)
       │
       │ GET /api/auth/sso
       │──────────────────────────────────────────────┐
       │← 200 OK + {ssoUrl: "..."}                      │
       │                                                │
       │ Proceso de ssoUrl                              │
       │──────────────────────────────────────────────→ │
       │                      GET /api/auth/sso/callback│
       │←───────────────────────────────────────────────┘
       │              200 OK + JWT Tokens
```

### Capas de la Aplicación

| Capa | Responsabilidad |
|------|-----------------|
| **Controller** | Manejo de peticiones HTTP, validación de entrada |
| **Service** | Lógica de negocio, orquestación |
| **Repository** | Acceso a datos con Spring Data JPA |
| **Filter** | Intercepción de peticiones, validación JWT |
| **Exception Handler** | Manejo centralizado de errores |
| **Util** | Funciones transversales (JWT, Logging) |

---

## 🚀 Ejecución Local

### Requisitos Previos
- Java 1.8 JDK
- Maven 3.8.8

### Pasos

```bash
# 1. Clonar repositorio
git clone [url-repositorio]
cd auth-backend

# 2. Compilar
mvn clean compile

# 3. Ejecutar tests
mvn test

# 4. Iniciar aplicación
mvn spring-boot:run

# 5. Verificar endpoints
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@econocom.com","password":"password123"}'

# 6. Acceder a consola H2 (opcional)
# http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:authdb
```

---

## 📊 Características Implementadas

- ✅ Autenticación JWT con access token y refresh token
- ✅ Base de datos H2 en memoria con JPA
- ✅ Gestión de errores centralizada con `GlobalExceptionHandler`
- ✅ Logging estandarizado con formato consistente (y escritura en archivo)
- ✅ Flujo SSO simulado (redirección mediante 200 OK + URL en body)
- ✅ CORS configurado para comunicación con Angular
- ✅ Pruebas unitarias e integración con JUnit 4
- ✅ Cobertura de código con JaCoCo (objetivo: 70%+)
- ✅ Consola H2 para desarrollo y debugging

---

## 📚 Referencias

- [Prueba Técnica Econocom 2025](../docs/Prueba_tecnica_Angular+Spring_Boot.pdf)
- [Spring Boot 2.7 Documentation](https://docs.spring.io/spring-boot/docs/2.7.x/reference/html/)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [JUnit 4 Documentation](https://junit.org/junit4/)

---

**Desarrollado para:** Econocom Live Tech  
**Año:** 2026