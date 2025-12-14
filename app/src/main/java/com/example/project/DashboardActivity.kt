package com.example.project

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.project.ui.theme.ProjectTheme
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Dashboard - Registro de Incidentes
 * Los usuarios pueden reportar incidentes con foto, ubicación y descripción
 */
class DashboardActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectTheme {
                DashboardScreen()
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun DashboardScreen() {
        val context = LocalContext.current
        var descripcion by remember { mutableStateOf("") }
        var fotoUri by remember { mutableStateOf<Uri?>(null) }
        var ubicacion by remember { mutableStateOf<Pair<Double, Double>?>(null) }
        var cargando by remember { mutableStateOf(false) }
        var mensajeUbicacion by remember { mutableStateOf("Ubicación no capturada") }
        
        // Launcher para tomar foto
        val tomarFotoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { exitoso ->
            if (exitoso) {
                Toast.makeText(context, "Foto capturada", Toast.LENGTH_SHORT).show()
            } else {
                fotoUri = null
            }
        }
        
        // Launcher para permisos de cámara
        val permisoCamaraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { otorgado ->
            if (otorgado) {
                abrirCamara(tomarFotoLauncher) { uri -> fotoUri = uri }
            } else {
                Toast.makeText(context, "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Launcher para permisos de ubicación
        val permisoUbicacionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permisos ->
            if (permisos[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permisos[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                obtenerUbicacion { lat, lon ->
                    ubicacion = Pair(lat, lon)
                    mensajeUbicacion = "📍 Ubicación capturada"
                }
            } else {
                Toast.makeText(context, "Se necesita permiso de ubicación", Toast.LENGTH_SHORT).show()
            }
        }
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título
                Text(
                    text = "Registrar Incidente",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Email del usuario
                val usuario = FirebaseConfig.getAuth().currentUser
                Text(
                    text = usuario?.email ?: "Usuario",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Campo de descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción del incidente") },
                    placeholder = { Text("Ej: Bache en la calle, semáforo dañado...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    enabled = !cargando,
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Estado de la foto
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (fotoUri != null) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = if (fotoUri != null) "✅ Foto capturada" else "📷 Sin foto",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Estado de la ubicación
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (ubicacion != null) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = mensajeUbicacion,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botón para tomar foto
                Button(
                    onClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                                abrirCamara(tomarFotoLauncher) { uri -> fotoUri = uri }
                            }
                            else -> permisoCamaraLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !cargando
                ) {
                    Text("📷 Tomar Foto")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Botón para capturar ubicación
                OutlinedButton(
                    onClick = {
                        val permisos = arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        
                        when {
                            permisos.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED } -> {
                                obtenerUbicacion { lat, lon ->
                                    ubicacion = Pair(lat, lon)
                                    mensajeUbicacion = "📍 Ubicación capturada"
                                }
                            }
                            else -> permisoUbicacionLauncher.launch(permisos)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !cargando
                ) {
                    Text("📍 Capturar Ubicación")
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botón para guardar incidente
                Button(
                    onClick = {
                        when {
                            descripcion.isEmpty() -> {
                                Toast.makeText(context, "Agrega una descripción", Toast.LENGTH_SHORT).show()
                            }
                            fotoUri == null -> {
                                Toast.makeText(context, "Toma una foto del incidente", Toast.LENGTH_SHORT).show()
                            }
                            ubicacion == null -> {
                                Toast.makeText(context, "Captura la ubicación", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                guardarIncidente(
                                    descripcion = descripcion,
                                    fotoUri = fotoUri!!,
                                    ubicacion = ubicacion!!,
                                    onLoading = { cargando = it },
                                    onSuccess = {
                                        descripcion = ""
                                        fotoUri = null
                                        ubicacion = null
                                        mensajeUbicacion = "Ubicación no capturada"
                                        Toast.makeText(context, "✅ Incidente guardado", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("💾 Guardar Incidente", style = MaterialTheme.typography.titleMedium)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón para ver lista de incidentes
                OutlinedButton(
                    onClick = {
                        startActivity(Intent(context, ListaIncidentesActivity::class.java))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !cargando
                ) {
                    Text("📋 Ver Todos los Incidentes")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón de cerrar sesión
                TextButton(
                    onClick = {
                        FirebaseConfig.getAuth().signOut()
                        startActivity(Intent(context, LoginActivity::class.java))
                        finish()
                    },
                    enabled = !cargando
                ) {
                    Text("Cerrar Sesión")
                }
            }
        }
    }
    
    /**
     * Abre la cámara para tomar foto
     */
    private fun abrirCamara(
        launcher: androidx.activity.result.ActivityResultLauncher<Uri>,
        onUriCreated: (Uri) -> Unit
    ) {
        try {
            val photoFile = File(cacheDir, "incidente_${System.currentTimeMillis()}.jpg")
            val photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            onUriCreated(photoUri)
            launcher.launch(photoUri)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Obtiene la ubicación actual del dispositivo
     */
    @SuppressWarnings("MissingPermission")
    private fun obtenerUbicacion(onUbicacion: (Double, Double) -> Unit) {
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onUbicacion(location.latitude, location.longitude)
                    Toast.makeText(this, "Ubicación obtenida", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No se pudo obtener ubicación", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error de ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Guarda el incidente en Firebase Database con foto en base64
     */
    private fun guardarIncidente(
        descripcion: String,
        fotoUri: Uri,
        ubicacion: Pair<Double, Double>,
        onLoading: (Boolean) -> Unit,
        onSuccess: () -> Unit
    ) {
        onLoading(true)
        
        val usuario = FirebaseConfig.getAuth().currentUser ?: return
        val incidenteId = FirebaseDatabase.getInstance().reference.child("incidentes").push().key ?: return
        
        try {
            // 1. Convertir foto a base64
            val fotoBase64 = convertirImagenABase64(fotoUri)
            
            if (fotoBase64.isEmpty()) {
                onLoading(false)
                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                return
            }
            
            // 2. Crear objeto Incidente con base64
            val incidente = Incidente(
                id = incidenteId,
                descripcion = descripcion,
                fotoUrl = fotoBase64, // Ahora guardamos base64 en lugar de URL
                latitud = ubicacion.first,
                longitud = ubicacion.second,
                fecha = System.currentTimeMillis(),
                usuarioEmail = usuario.email ?: "",
                usuarioId = usuario.uid
            )
            
            // 3. Guardar en Firebase Database
            FirebaseDatabase.getInstance().reference
                .child("incidentes")
                .child(incidenteId)
                .setValue(incidente.toMap())
                .addOnSuccessListener {
                    onLoading(false)
                    onSuccess()
                }
                .addOnFailureListener { error ->
                    onLoading(false)
                    Toast.makeText(this, "Error al guardar: ${error.message}", Toast.LENGTH_LONG).show()
                }
                
        } catch (e: Exception) {
            onLoading(false)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Convierte una imagen URI a String base64 optimizada
     */
    private fun convertirImagenABase64(uri: Uri): String {
        return try {
            // Leer imagen desde URI
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // Redimensionar para optimizar (máximo 800px en el lado más grande)
            val maxDimension = 800
            val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            
            val resizedBitmap = if (scale < 1) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt(),
                    (bitmap.height * scale).toInt(),
                    true
                )
            } else {
                bitmap
            }
            
            // Comprimir a JPEG con calidad 70
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val imageBytes = outputStream.toByteArray()
            
            // Convertir a base64
            Base64.encodeToString(imageBytes, Base64.DEFAULT)
            
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
