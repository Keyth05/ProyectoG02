package com.example.project

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.ui.theme.ProjectTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

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
        var incidentes by remember { mutableStateOf<List<Incidente>>(emptyList()) }
        var cargando by remember { mutableStateOf(true) }

        val fondoColor = MaterialTheme.colorScheme.background
        val primario = MaterialTheme.colorScheme.primary
        val onPrimario = MaterialTheme.colorScheme.onPrimary

        LaunchedEffect(Unit) {
            cargarIncidentes { lista ->
                incidentes = lista.sortedByDescending { it.fecha }
                cargando = false
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "🚨 Incidentes 🚨",
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            letterSpacing = 1.sp,
                            color = onPrimario
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = primario
                    ),
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Text("🔙", fontSize = 24.sp, color = onPrimario)
                        }
                    }
                )
            },
            containerColor = fondoColor
        ) { padding ->
            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                }
            } else if (incidentes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🛡️", fontSize = 60.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Zona segura...",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "No hay incidentes reportados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { finish() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Reportar peligro ⚠️", modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(incidentes) { item ->
                        IncidentePeligroCard(item)
                    }
                }
            }
        }
    }

    @Composable
    fun IncidentePeligroCard(incidente: Incidente) {
        val context = LocalContext.current
        val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        val fecha = dateFormat.format(Date(incidente.fecha))

        val bitmap = remember(incidente.fotoUrl) {
            try {
                if (incidente.fotoUrl.isNotEmpty()) {
                    val bytes = Base64.decode(incidente.fotoUrl, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } else null
            } catch (e: Exception) { null }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color.Red
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column {
                // FOTO
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFEEEEEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⚠️", fontSize = 40.sp)
                                Text("Sin evidencia", color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Chip de Fecha
                    Surface(
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFD32F2F),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "📅 $fecha",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // CONTENIDO
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            "📢",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = incidente.descripcion,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Usuario
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👁️", fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Testigo: ${incidente.usuarioEmail.substringBefore("@")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Ver Ubicación
                    OutlinedButton(
                        onClick = {
                            val uri = "geo:${incidente.latitud},${incidente.longitud}?q=${incidente.latitud},${incidente.longitud}"
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error al abrir mapa", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        // CORRECCIÓN AQUÍ: Uso estándar de BorderStroke
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text("📍 VER EN EL MAPA", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }

    private fun cargarIncidentes(onIncidentes: (List<Incidente>) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference.child("incidentes")
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Incidente>()
                for (child in snapshot.children) {
                    child.getValue(Incidente::class.java)?.let { lista.add(it) }
                }
                onIncidentes(lista)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}