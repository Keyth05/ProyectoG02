package com.example.project

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.project.ui.theme.ProjectTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

/**
 * Lista de Incidentes - Muestra todos los incidentes reportados por todos los usuarios
 */
class ListaIncidentesActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectTheme {
                ListaIncidentesScreen()
            }
        }
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ListaIncidentesScreen() {
        val context = LocalContext.current
        var incidentes by remember { mutableStateOf<List<Incidente>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }
        
        // Cargar incidentes de Firebase
        LaunchedEffect(Unit) {
            cargarIncidentes { listaIncidentes ->
                incidentes = listaIncidentes.sortedByDescending { it.fecha }
                cargando = false
            }
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Incidentes Reportados") },
                    navigationIcon = {
                        TextButton(onClick = { finish() }) {
                            Text("← Volver")
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (incidentes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No hay incidentes reportados",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { finish() }) {
                            Text("Reportar Primer Incidente")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(incidentes) { incidente ->
                        IncidenteCard(incidente = incidente)
                    }
                }
            }
        }
    }
    
    @Composable
    fun IncidenteCard(incidente: Incidente) {
        val context = LocalContext.current
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaFormateada = dateFormat.format(Date(incidente.fecha))
        
        // Decodificar imagen fuera del composable
        val bitmap = remember(incidente.fotoUrl) {
            try {
                if (incidente.fotoUrl.isNotEmpty()) {
                    val imageBytes = Base64.decode(incidente.fotoUrl, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Foto del incidente desde base64
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Foto del incidente",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                } else if (incidente.fotoUrl.isNotEmpty()) {
                    // Si hay error decodificando, mostrar mensaje
                    Text(
                        text = "Error al cargar imagen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                
                // Descripción
                Text(
                    text = incidente.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Información del usuario
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "👤 ${incidente.usuarioEmail}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "📅 $fechaFormateada",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Botón para ver ubicación en mapa
                OutlinedButton(
                    onClick = {
                        // Abrir ubicación en Google Maps
                        val uri = "geo:${incidente.latitud},${incidente.longitud}?q=${incidente.latitud},${incidente.longitud}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "No se pudo abrir el mapa",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📍 Ver Ubicación en Mapa")
                }
            }
        }
    }
    
    /**
     * Carga todos los incidentes desde Firebase
     */
    private fun cargarIncidentes(onIncidentes: (List<Incidente>) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference.child("incidentes")
        
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaIncidentes = mutableListOf<Incidente>()
                
                for (incidenteSnapshot in snapshot.children) {
                    try {
                        val incidente = incidenteSnapshot.getValue(Incidente::class.java)
                        incidente?.let { listaIncidentes.add(it) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                onIncidentes(listaIncidentes)
            }
            
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ListaIncidentesActivity,
                    "Error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
