# Mi Tiendita · App CRUD con autenticación

Proyecto Unidad 3 · Desarrollo de aplicaciones móviles (Android nativo, Kotlin + Jetpack Compose).

**Dominio:** Tienda de abarrotes · **Recurso principal:** Producto

| Atributo | Tipo | Validación |
|---|---|---|
| `id` | Int | Lo asigna la API |
| `name` (nombre) | Texto | Obligatorio, mínimo 3 caracteres |
| `category` (categoría) | Texto | Obligatoria, se elige de una lista |
| `price` (precio) | Numérico (Double) | Obligatorio, > 0, ≤ 100,000, máximo 2 decimales |
| `stock` (existencias) | Numérico (Int) | Obligatorio, entero ≥ 0 |
| `expirationDate` (caducidad) | Fecha (LocalDate) | Obligatoria; un producto nuevo no puede estar caducado |

## Cómo ejecutarlo

### 1. Backend (json-server + json-server-auth)

Requiere Node.js.

```bash
cd backend
npm install
npm start
```

La API queda en `http://localhost:3000`. Para empezar con datos limpios, copia `db.seed.json` sobre `db.json`.

| Método | Ruta | Uso |
|---|---|---|
| POST | `/register` | Crear cuenta → `{ accessToken, user }` |
| POST | `/login` | Autenticar → `{ accessToken, user }` |
| GET | `/products` | Listar |
| GET | `/products/{id}` | Obtener uno |
| POST | `/products` | Crear |
| PUT | `/products/{id}` | Actualizar |
| DELETE | `/products/{id}` | Eliminar |

`/products` está protegido (`routes.json` → 660): sin `Authorization: Bearer <token>` responde 401.

### 2. App Android

1. Abre la carpeta del proyecto en Android Studio y sincroniza Gradle.
2. Ejecuta en un **emulador**: la app usa `http://10.0.2.2:3000/` (la PC vista desde el emulador).
3. En un **celular físico** (misma red Wi-Fi), cambia `BASE_URL` en `app/build.gradle.kts` por la IP de tu PC, por ejemplo `http://192.168.1.70:3000/`.

El tráfico HTTP sin cifrar solo está permitido en la variante **debug** (`app/src/debug/res/xml/network_security_config.xml`).

## Arquitectura

MVVM + Clean Architecture por feature, con inyección de dependencias manual.

```
com.upchiapas.mitiendita
├── TiendaApplication        → crea el AppContainer una sola vez
├── MainActivity / MainViewModel → decide la pantalla inicial (sesión activa = Home)
├── core
│   ├── di          → AppContainer (Retrofit, OkHttp, sesión, repositorios) + acceso desde factories
│   ├── network     → AuthInterceptor (Bearer token) y safeApiCall (errores → AppException)
│   ├── session     → SessionStorage (interfaz) + SharedPrefsSessionStorage
│   ├── error       → AppException (errores en lenguaje del dominio)
│   ├── validation  → reglas compartidas (correo, longitud de contraseña)
│   └── ui          → componentes reutilizables (carga, error, vacío, diálogos)
├── auth
│   ├── domain      → User, AuthRepository (interfaz)
│   ├── data        → AuthApi, DTOs @Serializable, AuthMapper, AuthRepositoryImpl
│   └── presentation→ Login y Register (UiState + ViewModel + Screen)
├── products
│   ├── domain      → Product, ProductCategories, ProductRepository (interfaz)
│   ├── data        → ProductApi, DTOs @Serializable, ProductMapper, ProductRepositoryImpl
│   └── presentation→ list (Home), detail, form (crear/editar reutilizable)
└── navigation      → rutas type-safe y NavHost
```

- **UI:** Jetpack Compose + Material 3, sin XML para pantallas. Cada pantalla tiene una parte *stateful* (conecta el ViewModel) y una *stateless* (solo dibuja).
- **Estado:** `MutableStateFlow` privado + `StateFlow<UiState>` inmutable, observado con `collectAsStateWithLifecycle()`.
- **Red:** Retrofit 3 + kotlinx.serialization + OkHttp con interceptor de token.
- **Repository:** las interfaces viven en `domain` y exponen solo modelos de dominio; los DTOs no salen de `data`.
- **DI manual:** `AppContainer` en `TiendaApplication`; los ViewModels se crean con `viewModelFactory { initializer { … } }`.
- **Lista siempre actualizada:** `ProductRepository.products` es un `StateFlow` que se actualiza tras crear, editar o eliminar.
- **Sesión:** el token se guarda en SharedPreferences detrás de `SessionStorage`. Si la API responde 401 (token vencido, dura 1 h), la app cierra sesión y regresa al login con un aviso.
