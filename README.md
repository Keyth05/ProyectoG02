# 📌 Sistema de Reporte de Incidentes Urbanos

Aplicación Android para que ciudadanos reporten y visualicen incidentes urbanos con foto, ubicación GPS y descripción. Los incidentes se comparten en tiempo real entre todos los usuarios mediante Firebase.

Problema: los ciudadanos no siempre tienen un canal rápido, documentado y geolocalizado para reportar problemas en la vía pública como:

- Baches
- Luminarias dañadas
- Accidentes de tránsito
- Actos vandálicos
- Zonas inseguras

Esta aplicación permite capturar evidencia (foto), registrar ubicación y enviar un reporte que quede almacenado y disponible para revisión.

**Imágenes y almacenamiento:**

Las fotos se optimizan automáticamente (máximo 800px, compresión JPEG 70%), se convierten a Base64 y se guardan directamente en Firebase Realtime Database en el campo `fotoBase64`. Este es el enfoque elegido para este proyecto.

Ventajas: simple y sin necesidad de configurar Firebase Storage.
Desventajas: no es ideal para imágenes muy grandes y puede inflar la base de datos; si en el futuro decides migrar a Storage, la estructura debe cambiar para almacenar URL en lugar de Base64.

---

## 🎯 Funcionalidades

### ✨ Autenticación
- **Login**: Inicio de sesión con email y contraseña
- **Registro**: Crear nueva cuenta con validación de contraseña (mínimo 6 caracteres)
- **Sesión persistente**: Recuerda usuario logueado

### 📝 Registrar Incidentes
- Descripción del incidente
- Captura de foto con cámara (optimizada)
- Ubicación GPS automática
- Guardado en Firebase Realtime Database (y opcionalmente Firebase Storage para las imágenes)
- Validación de campos requeridos

### 📋 Ver Incidentes
- Lista de todos los incidentes reportados
- Visible para todos los usuarios autenticados
- Información incluida:
  - Foto (o URL de Storage)
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
3. Agrega una app Android con el nombre de paquete correspondiente a tu aplicación
4. Descarga el archivo `google-services.json`
5. Colócalo en `app/` (es decir `app/google-services.json`)

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

> Si optas por usar Firebase Storage, habilítalo en Firebase Console → Storage y aplica reglas adecuadas (ver sección de arriba).

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
| **FirebaseConfig.kt** | Configuración centralizada de Firebase. Inicializa Auth y Database |
| **MyApp.kt** | Clase Application que inicializa Firebase al arrancar la app |
| **Incidente.kt** | Modelo de datos para representar un incidente (descripción, fotoUrl, ubicación, fecha, usuario) |

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
      ├── fotoBase64: String (Base64 optimizada de la imagen)
      ├── latitud: Double
      ├── longitud: Double
      ├── fecha: Long (timestamp)
      ├── usuarioEmail: String
      └── usuarioId: String
```

**Nota:** Las fotos se optimizan automáticamente (máximo 800px, compresión JPEG 70%) antes de guardarse como Base64.

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
// Firebase (Auth, Database)
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
   │  ├─ Convertir imagen a Base64 y guardar en Realtime Database (campo `fotoBase64`)
   │  └─ Guardar todo en Realtime Database (incluyendo fotoBase64)
   └─ Ver lista → ListaIncidentesActivity

3. ListaIncidentesActivity
   ├─ Cargar incidentes de Firebase
   ├─ Cargar imágenes desde URL o decodificar Base64 si corresponde
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
- `Image` (Compose) para renderizar imagen desde URL o Base64
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
✅ Revisa los logs para ver errores de decodificación o de Storage
✅ Si usas Storage, verifica reglas y permisos del bucket

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

**Storage:** removed — este proyecto no usa Firebase Storage; las imágenes se guardan en Realtime Database como Base64.

---

## 📝 Notas Importantes

- Las fotos se optimizan automáticamente antes de guardarse (max 800px, JPEG 70%)
- Puedes guardar imágenes como Base64 en Realtime Database (útil para prototipos) o usar Firebase Storage y guardar sólo la URL en la base de datos (recomendado para producción)
- La contraseña debe tener mínimo 6 caracteres (requisito de Firebase)
- Los incidentes son visibles para todos los usuarios autenticados
- La app mantiene la sesión activa hasta cerrar sesión manualmente

---

## 🎓 Tecnologías Utilizadas

- **Lenguaje**: Kotlin
- **UI Framework**: Jetpack Compose
- **Arquitectura**: MVVM implícito con State Hoisting
- **Backend**: Firebase (Auth, Realtime Database, Storage opcional)
- **Cámara**: FileProvider + Activity Result API
- **Ubicación**: Google Location Services (Fused Location Provider)
- **Carga de imágenes**: Coil

---



## 👨‍💻 Autor
keithyaguana@gmail.com
Proyecto creado como ejemplo de aplicación Android con Firebase.

---

