# 📱 Sistema de Registro de Incidentes

Aplicación Android para reportar y visualizar incidentes con foto, ubicación GPS y descripción. Los incidentes se comparten en tiempo real entre todos los usuarios a través de Firebase.

**✨ Las fotos se guardan en formato base64 optimizado directamente en Realtime Database (sin necesidad de Firebase Storage).**

## 🎯 Funcionalidades

### ✨ Autenticación
- **Login**: Inicio de sesión con email y contraseña
- **Registro**: Crear nueva cuenta con validación de contraseña (mínimo 6 caracteres)
- **Sesión persistente**: Recuerda usuario logueado

### 📝 Registrar Incidentes
- Descripción del incidente
- Captura de foto con cámara (optimizada y convertida a base64)
- Ubicación GPS automática
- Guardado en Firebase Realtime Database
- Validación de campos requeridos

### 📋 Ver Incidentes
- Lista de todos los incidentes reportados
- Visible para todos los usuarios
- Información incluida:
  - Foto del incidente
  - Descripción
  - Usuario que reportó
  - Fecha y hora
  - Ubicación (ver en Google Maps)
- Actualización en tiempo real

---

## 🔧 Configuración Inicial

### 1. Descargar google-services.json

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto o crea uno nuevo
3. Agrega una app Android:
   - **Nombre del paquete**: `com.example.project` (exactamente así)
   - Descarga el archivo `google-services.json`
4. Coloca el archivo en:
   ```
   /Users/kayaguana/Desktop/project/app/google-services.json
   ```

### 2. Habilitar Firebase Authentication

1. En Firebase Console → **Authentication**
2. Click en **"Comenzar"**
3. Habilita **"Correo electrónico/contraseña"**

### 3. Configurar Firebase Realtime Database

1. En Firebase Console → **Realtime Database**
2. Click en **"Crear base de datos"**
3. Selecciona tu ubicación
4. Inicia en **modo de prueba** (o usa estas reglas):

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

### 4. Sincronizar y Compilar

En Android Studio:
1. **Sync Project with Gradle Files**
2. **Build** → **Clean Project**
3. **Build** → **Rebuild Project**
4. **Run** → Ejecutar en dispositivo/emulador

---

## 📁 Estructura del Proyecto

### 📂 Archivos Principales

#### `app/src/main/java/com/example/project/`

| Archivo | Descripción |
|---------|-------------|
| **MainActivity.kt** | Punto de entrada de la app. Verifica si hay sesión activa y redirige a Login o Dashboard |
| **LoginActivity.kt** | Pantalla de inicio de sesión con email y contraseña |
| **RegisterActivity.kt** | Pantalla de registro para nuevos usuarios. Valida contraseña (min 6 caracteres) |
| **DashboardActivity.kt** | Pantalla principal para registrar incidentes. Captura foto, ubicación y descripción |
| **ListaIncidentesActivity.kt** | Muestra todos los incidentes de todos los usuarios en tiempo real |
| **FirebaseConfig.kt** | Configuración centralizada de Firebase. Inicializa Auth, Database y Storage |
| **MyApp.kt** | Clase Application que inicializa Firebase al arrancar la app |
| **Incidente.kt** | Modelo de datos para representar un incidente (descripción, foto, ubicación, fecha, usuario) |

#### `app/src/main/res/xml/`

| Archivo | Descripción |
|---------|-------------|
| **file_paths.xml** | Configuración del FileProvider para compartir archivos de la cámara |

### 🗂️ Estructura de Datos en Firebase

#### Realtime Database
```
incidentes/
  └── {incidente-id-generado}/
      ├── id: String
      ├── descripcion: String
      ├── fotoUrl: String (imagen en base64 optimizada)
      ├── latitud: Double
      ├── longitud: Double
      ├── fecha: Long (timestamp)
      ├── usuarioEmail: String
      └── usuarioId: String
```

**Nota:** Las fotos se optimizan automáticamente (máximo 800px, compresión JPEG 70%) antes de convertirse a base64.

---

## 🔑 Permisos Requeridos

La app solicita automáticamente estos permisos:

- **CAMERA**: Para tomar fotos de los incidentes
- **ACCESS_FINE_LOCATION**: Para obtener ubicación GPS precisa
- **ACCESS_COARSE_LOCATION**: Para ubicación aproximada
- **INTERNET**: Para conectarse a Firebase

---

## 📦 Dependencias Clave

```kotlin
// Firebase (solo Auth y Database, sin Storage)
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-database-ktx")

// CameraX
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")

// Google Play Services (Ubicación)
implementation("com.google.android.gms:play-services-location:21.0.1")

// Jetpack Compose + Material3
implementation(libs.androidx.compose.material3)
```

---

## 🚀 Flujo de la Aplicación

```
1. MainActivity
   ├─ Usuario logueado? → DashboardActivity
   └─ No logueado? → LoginActivity
      └─ Sin cuenta? → RegisterActivity

2. DashboardActivity (Registrar Incidente)
   ├─ Agregar descripción
   ├─ Tomar foto (FileProvider + CameraX)
   ├─ Capturar ubicación GPS (Google Location Services)
   ├─ Guardar en Firebase:
   │  ├─ Optimizar imagen (redimensionar a 800px max)
   │  ├─ Comprimir a JPEG 70%
   │  ├─ Convertir a base64
   │  └─ Guardar todo en Realtime Database
   └─ Ver lista → ListaIncidentesActivity

3. ListaIncidentesActivity
   ├─ Cargar incidentes de Firebase
   ├─ Decodificar imágenes base64
   ├─ Mostrar en LazyColumn
   └─ Abrir ubicación en Google Maps
```

---

## 🎨 Componentes de UI

### DashboardActivity
- `OutlinedTextField` para descripción
- `Card` para indicadores de estado (foto/ubicación)
- `Button` para tomar foto
- `OutlinedButton` para capturar ubicación
- `Button` con loading para guardar incidente

### ListaIncidentesActivity
- `TopAppBar` con navegación
- `LazyColumn` para lista de incidentes
- `Image` (Compose) para renderizar base64
- `OutlinedButton` para abrir Google Maps

---

## 🐛 Solución de Problemas

### Error: "API key not valid"
✅ Descarga `google-services.json` de Firebase Console y colócalo en `app/`

### Error: "Firebase not initialized"
✅ Verifica que `MyApp` esté declarado en `AndroidManifest.xml` con `android:name=".MyApp"`

### No se obtiene ubicación
✅ Verifica permisos de ubicación en Configuración del dispositivo
✅ En emulador, configura una ubicación GPS simulada

### Las fotos no se cargan
✅ Verifica que la imagen se haya capturado correctamente
✅ Revisa los logs para ver errores de decodificación base64
✅ Las imágenes muy grandes se optimizan automáticamente

### No se ven los incidentes
✅ Verifica que Realtime Database esté habilitado
✅ Verifica las reglas de Database

---

## 📱 Requisitos del Sistema

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Compile SDK**: 36
- **JVM Target**: 11
- **Kotlin**: 2.0.21
- **Gradle**: 8.13.2

---

## 🔐 Seguridad

### Reglas de Firebase (Producción)

Para producción, actualiza las reglas:

**Realtime Database:**
```json
{
  "rules": {
    "incidentes": {
      ".read": "auth != null",
      "$incidenteId": {
        ".write": "auth != null && !data.exists()",
        ".validate": "newData.hasChildren(['id', 'descripcion', 'fotoUrl', 'latitud', 'longitud', 'fecha', 'usuarioEmail', 'usuarioId'])"
      }
    }
  }
}
```

**Nota:** Las imágenes en base64 se validan automáticamente al estar dentro de la estructura de datos.

---

## 📝 Notas Importantes

- Las fotos se optimizan automáticamente antes de guardarse (max 800px, JPEG 70%)
- Las imágenes se convierten a base64 y se guardan directamente en Realtime Database
- **No se requiere Firebase Storage** (ahorra costos)
- La contraseña debe tener mínimo 6 caracteres (requisito de Firebase)
- Los incidentes son visibles para todos los usuarios autenticados
- La app mantiene la sesión activa hasta cerrar sesión manualmente
- Base64 es ideal para imágenes pequeñas/medianas optimizadas

---

## 🎓 Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose
- **Arquitectura**: MVVM implícito con State Hoisting
- **Backend**: Firebase (Auth, Realtime Database, Storage)
- **Cámara**: FileProvider + Activity Result API
- **Ubicación**: Google Location Services (Fused Location Provider)
- **Carga de imágenes**: Coil

---

## 📄 Licencia

Este es un proyecto educativo simple para demostrar integración con Firebase.

---

## 👨‍💻 Autor

Proyecto creado como ejemplo de aplicación Android con Firebase.

---

## 🚀 Próximas Mejoras (Opcionales)

- [ ] Filtrar incidentes por fecha o usuario
- [ ] Permitir editar/eliminar propios incidentes
- [ ] Agregar comentarios a incidentes
- [ ] Notificaciones push de nuevos incidentes
- [ ] Categorías de incidentes
- [ ] Búsqueda por ubicación cercana
- [ ] Modo offline con sincronización
- [ ] Subir múltiples fotos por incidente

---


