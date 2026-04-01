# Proyecto de Autenticación - Econocom Live Tech

## 📋 Resumen Ejecutivo

Sistema de autenticación full-stack desarrollado como prueba técnica para **Econocom Live Tech (2026)**. Implementa autenticación JWT con soporte para login tradicional (email/password) y Single Sign-On (SSO) simulado.

| Aspecto | Detalle |
|---------|---------|
| **Stack Backend** | Spring Boot 2.7.18, Java 1.8, H2 Database |
| **Stack Frontend** | Angular 16.2.16, Angular Material 16.2.14 |
| **Seguridad** | JWT (JSON Web Tokens), Spring Security |
| **Base de Datos** | H2 (desarrollo/tests), JPA/Hibernate |
| **Pruebas** | JUnit 4 (backend), Jasmine (frontend) |
| **Tiempo Estimado** | 6-12 horas (con SSO) |

---

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────────────┐
│                      CLIENTE (BROWSER)                       │
│                   Angular 16 + Material                      │
│  ┌─────────────┐         ┌─────────────────────────────┐    │
│  │   /login    │         │      /sso/callback          │    │
│  │  Component  │         │       Component             │    │
│  └──────┬──────┘         └─────────────┬───────────────┘    │
└─────────┼──────────────────────────────┼────────────────────┘
          │                              │
          │ HTTP/REST                    │ window.location.href
          │                              │ (navegación completa)
          ▼                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    SERVIDOR (Spring Boot)                    │
│                      Puerto: 8080                            │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  AuthController                                        │ │
│  │  ├── POST /api/auth/login    → JWT Tokens              │ │
│  │  ├── GET  /api/auth/sso      → URL SSO (200 OK)*       │ │
│  │  └── GET  /api/auth/sso/callback → JWT Tokens          │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  JwtAuthenticationFilter (filtro de seguridad)         │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │  AuthServiceImpl + UsuarioRepository (JPA/H2)          │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

&gt; **\*Nota sobre SSO**: Se implementa redirección mediante **200 OK + URL en body** en lugar de **302 Redirect**, porque los navegadores siguen automáticamente las redirecciones HTTP en peticiones AJAX, impidiendo gestionar el flujo desde Angular.

---

## 📦 Estructura de Repositorio

```
econocom-auth-project/
├── auth-backend/              # Spring Boot (Java 1.8)
│   ├── src/
│   │   ├── main/java/...      # Código fuente
│   │   ├── main/resources/    # Configuración, data.sql
│   │   └── test/java/...      # Tests JUnit 4
│   ├── pom.xml                # Maven, dependencias
│   └── README-BACKEND.md      # Documentación detallada
│
├── auth-frontend/             # Angular 16
│   ├── src/app/               # Componentes, servicios
│   ├── src/assets/            # Imágenes, recursos
│   ├── angular.json           # Configuración CLI
│   ├── package.json           # NPM dependencies
│   └── README-FRONTEND.md     # Documentación detallada
│
└── README-GENERAL.md          # Este archivo
```

---

## 🚀 Guía de Ejecución Rápida

### 1. Backend (Spring Boot)

```bash
cd auth-backend

# Compilar y testear
mvn clean test

# Iniciar servidor
mvn spring-boot:run

# Servidor disponible en: http://localhost:8080
# Consola H2: http://localhost:8080/h2-console
```

### 2. Frontend (Angular)

```bash
cd auth-frontend

# Instalar dependencias
npm install

# Iniciar servidor de desarrollo
ng serve

# Aplicación disponible en: http://localhost:4200
```

### 3. Verificación

```bash
# Test login desde terminal
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@econocom.com","password":"password123"}'
```

---

## ✅ Requisitos Cumplidos

### Backend (Spring Boot)

| Requisito | Estado | Implementación |
|-----------|--------|----------------|
| Java 1.8 | ✅ | `maven.compiler.source/target: 1.8` |
| Spring Boot 2.7.18 | ✅ | Parent POM |
| API REST | ✅ | `AuthController` con endpoints `/api/auth/**` |
| JWT | ✅ | `JwtUtil` + `JwtAuthenticationFilter` |
| Base de Datos | ✅ | H2 en memoria + JPA |
| ORM (JPA/MyBatis) | ✅ | Spring Data JPA |
| SSO Simulado | ✅ | Endpoints `/sso` y `/sso/callback` |
| Manejo de Errores | ✅ | `GlobalExceptionHandler` |
| Pruebas Unitarias | ✅ | JUnit 4 (15+ tests) |
| Pruebas Integración | ✅ | Spring Boot Test (20+ tests) |

### Frontend (Angular)

| Requisito | Estado | Implementación |
|-----------|--------|----------------|
| Angular 16.2.16 | ✅ | `package.json` |
| Angular Material | ✅ | `MatFormField`, `MatButton`, etc. |
| Componente Login | ✅ | `LoginComponent` con formulario |
| Servicio Auth | ✅ | `AuthService` con HttpClient |
| Validaciones | ✅ | Reactive Forms + validadores |
| SSO Frontend | ✅ | Botón SSO + `SsoCallbackComponent` |
| Manejo de Errores | ✅ | Snackbar + mensajes informativos |
| Pruebas Unitarias | ✅ | Jasmine + Karma |

---

## 🧪 Estrategia de Pruebas

### Backend (JUnit 4)

| Tipo | Cantidad | Cobertura |
|------|----------|-----------|
| Unitarias | 15+ | JwtUtil, AuthService, Excepciones, Logging |
| Integración | 20+ | Controllers, Filtros, Repositorios, Configuración |
| End-to-End | 5+ | Flujos completos de autenticación |

&gt; **Nota**: Se seleccionó **JUnit 4** sobre JUnit 5 por mayor madurez y estabilidad con **Spring Boot 2.7.x** y **Java 1.8**, minimizando riesgos de incompatibilidad en el stack tecnológico requerido.

### Frontend (Jasmine)

| Tipo | Cantidad | Descripción |
|------|----------|-------------|
| Unitarias | Por componente | Componentes, Servicios, Interceptores |

---

## 🔐 Seguridad

### JWT (JSON Web Tokens)

- **Access Token**: Expira en 1 hora (`3600000 ms`)
- **Refresh Token**: Expira en 24 horas (`86400000 ms`)
- **Algoritmo**: HS256 (HMAC SHA-256)
- **Secret**: Configurable en `application.properties`

### CORS

Configurado para permitir comunicación entre:
- Frontend: `http://localhost:4200`
- Backend: `http://localhost:8080`

### Endpoints Públicos vs Protegidos

| Endpoint | Acceso | Descripción |
|----------|--------|-------------|
| `/api/auth/**` | Público | Login, SSO, Callback |
| `/h2-console/**` | Público | Consola de base de datos (dev) |
| Cualquier otro | Protegido | Requiere JWT válido |

---

## 📊 Decisiones Técnicas Destacadas

### 1. Base de Datos H2 (en memoria)

**Motivación**: Simplificar desarrollo y pruebas sin requerir instalación de MySQL/PostgreSQL.

**Ventajas**:
- Zero-config
- Consola web integrada
- Compatible JPA/Hibernate
- Datos iniciales vía `data.sql`

### 2. JUnit 4 en lugar de JUnit 5

**Motivación**: Mayor estabilidad con Spring Boot 2.7.x + Java 1.8.

**Justificación**: Aunque JUnit 5 es compatible con Java 8, JUnit 4 tiene historial más largo y documentación abundante para este stack específico, reduciendo riesgos de incompatibilidades sutiles.

### 3. SSO con 200 OK en lugar de 302 Redirect

**Motivación**: Limitación técnica de navegadores modernos.

**Explicación**: Los navegadores siguen automáticamente redirecciones HTTP 302 en peticiones `XMLHttpRequest`/`fetch`, impidiendo que Angular capture la URL de redirección. La solución implementada:
1. Backend devuelve 200 OK con JSON: `{"ssoUrl": "..."}`
2. Frontend recibe la URL y procesa los datos
3. Navegador realiza navegación completa (full page reload)
4. Callback SSO procesa el código y redirige de vuelta

---

## 📚 Documentación Adicional

| Documento | Contenido |
|-----------|-----------|
| [README-BACKEND.md](auth-backend/README-BACKEND.md) | Arquitectura backend, endpoints, configuración |
| [README-FRONTEND.md](auth-frontend/README-FRONTEND.md) | Componentes Angular, servicios, diseño |
| [Prueba Técnica PDF](docs/Prueba_tecnica_Angular+Spring_Boot.pdf) | Requisitos originales del proyecto |

---

## 🎯 Próximos Pasos (Recomendaciones)

### Para Producción

1. **Base de Datos**: Migrar H2 → PostgreSQL/MySQL
2. **Password Hashing**: Implementar BCrypt (actualmente en texto plano para pruebas)
3. **HTTPS**: Configurar SSL/TLS
4. **Refresh Token Rotation**: Rotar tokens en cada uso
5. **Rate Limiting**: Prevenir ataques de fuerza bruta

### Mejoras Técnicas

1. **Swagger/OpenAPI**: Documentación automática de API
2. **Docker**: Contenerización de ambos servicios
3. **CI/CD**: Pipeline de GitHub Actions/GitLab CI
4. **E2E Tests**: Cypress o Playwright para flujos completos

---

## 👥 Información del Proyecto

| Aspecto | Detalle |
|---------|---------|
| **Cliente** | Econocom Live Tech |
| **Año** | 2026 |
| **Tipo** | Prueba Técnica - Full Stack Developer |
| **Duración Estimada** | 6-12 horas |
| **Estado** | Completado ✅ |

---

**Repositorio Git**: [URL del repositorio]  
**Desarrollado por**: Guillermo Sanes Orrego  
**Fecha de entrega**: 01/04/2026

---

&gt; *"Implementación de un sistema de autenticación moderno, seguro y bien probado, cumpliendo con todos los requisitos técnicos especificados."*