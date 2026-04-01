# Auth Frontend - Angular

## 📋 Descripción

Frontend de autenticación desarrollado con **Angular 16.2.16** y **Angular Material 16.2.14** para la prueba técnica de Econocom. Proporciona interfaz de usuario para autenticación tradicional (email/password) y autenticación SSO simulada.

&gt; **Nota importante sobre SSO**: El flujo SSO implementa una redirección manual hacia la URL proporcionada por el backend, ya que los navegadores siguen automáticamente las redirecciones HTTP 302 en peticiones AJAX, lo que impedía gestionar el flujo desde Angular de forma controlada.

---

## 🛠️ Tecnologías y Versiones

| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| Angular | 16.2.16 | Framework principal (CLI 16.2.16) |
| TypeScript | 5.1.3 | Lenguaje de programación |
| Angular Material | 16.2.14 | Componentes UI (Material Design) |
| CDK | 16.2.14 | Component Dev Kit |
| RxJS | 7.8.0 | Programación reactiva |
| Jasmine | ~4.6.0 | Framework de pruebas unitarias |
| Karma | ~6.4.0 | Test runner para Angular |

---

## 📁 Estructura del Proyecto

```
auth-frontend/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── login/                    # Componente de login principal
│   │   │   │   ├── login.component.ts
│   │   │   │   ├── login.component.html
│   │   │   │   ├── login.component.scss
│   │   │   │   └── login.component.spec.ts
│   │   │   └── sso-callback/             # Componente de callback SSO
│   │   │       ├── sso-callback.component.ts
│   │   │       ├── sso-callback.component.html
│   │   │       ├── sso-callback.component.scss
│   │   │       └── sso-callback.component.spec.ts
│   │   ├── interceptors/
│   │   │   └── auth.interceptor.ts       # Interceptor HTTP para JWT
│   │   ├── services/
│   │   │   └── auth.service.ts           # Servicio de autenticación
│   │   ├── shared/
│   │   │   └── material/
│   │   │       └── material.module.ts    # Módulo de Angular Material
│   │   ├── app.component.ts              # Componente raíz
│   │   ├── app.component.html
│   │   ├── app.component.scss
│   │   ├── app.module.ts                 # Módulo principal
│   │   └── app-routing.module.ts         # Configuración de rutas
│   ├── assets/                           # Imágenes y recursos estáticos
│   ├── index.html
│   ├── main.ts
│   ├── styles.scss                       # Estilos globales
│   └── ...
├── angular.json                          # Configuración Angular CLI
├── package.json                          # Dependencias NPM
├── tsconfig.json                         # Configuración TypeScript
└── proxy.conf.json                       # Proxy para desarrollo local
```

---

## 🚀 Componentes Principales

### LoginComponent

**Ruta:** `/login`

**Funcionalidades:**
- Formulario de inicio de sesión con email y contraseña
- Validaciones de campos requeridos y formato de email
- Botón "ENTRAR" para autenticación tradicional
- Botón "Iniciar sesión con SSO" para autenticación SSO
- Visualización de errores del backend

**Diseño:**
- Implementado con Angular Material (MatFormField, MatInput, MatButton)
- Estilos SCSS siguiendo metodología BEM (recomendado)
- Responsive y accesible

### SsoCallbackComponent

**Ruta:** `/sso/callback`

**Funcionalidades:**
- Captura de parámetros `code` y `state` de la URL
- Envío de código al backend para validación
- Redirección a dashboard en caso de éxito
- Manejo de errores (código inválido, usuario no configurado)

---

## 🌐 Servicios

### AuthService

**Ubicación:** `src/app/services/auth.service.ts`

**Métodos:**

| Método | Descripción | Endpoint Backend |
|--------|-------------|------------------|
| `login(email, password)` | Autenticación tradicional | `POST /api/auth/login` |
| `initiateSso()` | Inicia flujo SSO | `GET /api/auth/sso` |
| `handleSsoCallback(code, state)` | Valida código SSO | `GET /api/auth/sso/callback` |
| `logout()` | Cierra sesión | - (local) |
| `isAuthenticated()` | Verifica si hay token válido | - (local) |

**Manejo de Tokens:**
- Almacenamiento en `localStorage`
- Interceptor HTTP añade token JWT en header `Authorization: Bearer ...`

---

## 🔧 Configuración

### Proxy de Desarrollo (`proxy.conf.json`)

```json
{
  "/api/*": {
    "target": "http://localhost:8080",
    "secure": false,
    "logLevel": "debug"
  }
}
```
---

## 🛣️ Rutas de la Aplicación

| Ruta | Componente | Descripción |
|------|------------|-------------|
| `/login` | `LoginComponent` | Página de inicio de sesión |
| `/sso/callback` | `SsoCallbackComponent` | Callback de autenticación SSO |
| `/` | redirect → `/login` | Redirección por defecto |
| `**` | redirect → `/login` | Ruta comodín (404) |

---

## 🧪 Pruebas

### Ejecución de Tests

```bash
# Ejecutar tests unitarios (Karma + Jasmine)
ng test

# Ejecutar con cobertura
ng test --code-coverage

# Ejecutar una sola vez (CI)
ng test --watch=false --browsers=ChromeHeadless
```

### Estrategia de Pruebas

| Tipo | Framework | Descripción |
|------|-----------|-------------|
| Unitarias | Jasmine + Karma | Componentes, Servicios, Pipes |
| E2E | (Opcional) Protractor/Cypress | Flujos completos de usuario |

### Estructura de Tests

```typescript
// Ejemplo: login.component.spec.ts
describe('LoginComponent', () =&gt; {
  // Test: Formulario inválido deshabilita botón
  // Test: Submit llama a AuthService.login()
  // Test: Error del backend muestra mensaje
  // Test: Éxito redirige a dashboard
});
```

&gt; **Nota**: Se utilizan **dobles de prueba (mocks)** para `AuthService` y `Router` en tests de componentes.

---

## 🎨 Diseño y Estilos

### Angular Material

Componentes utilizados:
- `MatCard` - Contenedor principal del login
- `MatFormField` + `MatInput` - Campos de formulario
- `MatButton` - Botones de acción (raised, color="primary")
- `MatProgressSpinner` - Indicador de carga
- `MatSnackBar` - Notificaciones de error

### SCSS / BEM

Se recomienda seguir metodología **BEM** (Block Element Modifier):

```scss
// login.component.scss
.login {
  &__container { /* ... */ }
  &__form { /* ... */ }
  &__input { /* ... */ }
  &__button {
    &--primary { /* ... */ }
    &--sso { /* ... */ }
  }
}
```

### Diseño Visual

- **Referencia:** Figma
- **Assets:** Carpeta `src/assets/` con imagen proporcionada
- **Colores:** Paleta de Material Design (primary: indigo, accent: pink)

---

## 🚀 Ejecución Local

### Requisitos Previos
- Node.js 16+ (recomendado 18)
- Angular CLI 16.2.16

```bash
# Verificar versiones
node -v      # v16+ o v18+
npm -v       # 8+
ng version   # Angular CLI 16.2.16
```

### Pasos

```bash
# 1. Instalar dependencias
npm install

# 2. Iniciar servidor de desarrollo
ng serve

# 3. Abrir navegador
# http://localhost:4200

# 4. Ejecutar tests
ng test

# 5. Compilar para producción
ng build --configuration production
# Output en: dist/auth-frontend/
```

---

## 🔗 Integración con Backend

### Flujo de Autenticación Tradicional

```
┌─────────────┐      POST /api/auth/login      ┌─────────────┐
│   Usuario   │ ──────────────────────────────→│   Backend   │
│  (Browser)  │  {email, password}             │  (Spring)   │
└─────────────┘                                └──────┬──────┘
       │←──────────────────────────────────────────────┘
       │         200 OK + {token, refreshToken}
       │
       │    Guardar en localStorage
       │    Redirigir a dashboard
```

### Flujo SSO (Simulado)

```
┌─────────────┐      GET /api/auth/sso         ┌─────────────┐
│   Usuario   │ ──────────────────────────────→│   Backend   │
│  (Angular)  │                                │  (Spring)   │
└─────────────┘                                └──────┬──────┘
       │←──────────────────────────────────────────────┘
       │         200 OK + {ssoUrl: "..."}
       │
       │                 ssoUrl
       │    (navegación completa del browser)
       │
       │    GET /api/auth/sso/callback?code=...&state=...
       │←──────────────────────────────────────────────┐
       │         200 OK + {token, refreshToken}        │
```

---

## 📊 Características Implementadas

- ✅ Formulario de login con Angular Material
- ✅ Validaciones de campos (requerido, formato email)
- ✅ Autenticación JWT con almacenamiento local
- ✅ Interceptor HTTP para añadir token automáticamente
- ✅ Flujo SSO simulado con redirección manual
- ✅ Componente de callback SSO con manejo de parámetros URL
- ✅ Manejo de errores del backend (snackbar)
- ✅ Pruebas unitarias con Jasmine
- ✅ Proxy de desarrollo para evitar CORS
- ✅ Diseño responsive con Material Design

---

## 📚 Referencias

- [Prueba Técnica Econocom 2026](../docs/Prueba_tecnica_Angular+Spring_Boot.pdf)
- [Angular 16 Documentation](https://angular.io/docs)
- [Angular Material 16](https://material.angular.io/)
- [Jasmine Documentation](https://jasmine.github.io/)
- [BEM Methodology](https://en.bem.info/methodology/)

---

**Desarrollado para:** Econocom Live Tech  
**Año:** 2026