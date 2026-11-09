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

### 1-B. Backend en AWS EC2 (AWS Academy)

1. En la consola de EC2, abre el **puerto 3000 (TCP)** en el grupo de seguridad de la instancia (regla de entrada, origen `0.0.0.0/0`).
2. Conéctate a la instancia (**Conectar → EC2 Instance Connect**) y ejecuta:

   ```bash
   curl -fsSL https://raw.githubusercontent.com/agdisc11/SH01_Aplicaci-n-CRUD/main/backend/deploy_ec2.sh | bash
   ```

   El script instala Node.js, descarga el proyecto en `/opt/mitiendita` y deja la API como servicio de systemd (`mitiendita-api`), así vuelve a arrancar sola si la instancia se reinicia.
3. Prueba desde el navegador `http://<IP-publica>:3000/products`: debe responder `"Missing authorization header"` (401), señal de que la API está arriba y protegida.

En AWS Academy la IP pública cambia cada vez que se reinicia el laboratorio; actualiza `API_BASE_URL` (abajo) cuando eso pase.

### 2. App Android

1. Abre la carpeta del proyecto en Android Studio y sincroniza Gradle.
2. La URL de la API se define en `gradle.properties`:

   ```properties
   API_BASE_URL=http://44.222.211.117:3000/   # API en EC2
   # API_BASE_URL=http://10.0.2.2:3000/       # API local + emulador
   ```

3. Con la API en EC2 la app funciona igual en el emulador y en un celular físico (con internet).

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
