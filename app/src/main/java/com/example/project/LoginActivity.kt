package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.ui.theme.ProjectTheme

/**
 * Pantalla de Login - Con un ícono simple y directo de incidente
 */
class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectTheme {
                LoginScreen()
            }
        }
    }

    @Composable
    fun LoginScreen() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var cargando by remember { mutableStateOf(false) }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // --- AQUÍ ESTÁ EL CAMBIO: ÍCONO PRINCIPAL ---
                IconoIncidente()

                Spacer(modifier = Modifier.height(32.dp))

                // Título de la App
                Text(
                    text = "Reporte Urbano",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )

                // Subtítulo explicativo
                Text(
                    text = "Tu ciudad, tus reportes",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                )

                // Campo de Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    enabled = !cargando,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Campo de Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    enabled = !cargando,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Botón de Login
                Button(
                    onClick = {
                        if (email.isNotEmpty() && password.isNotEmpty()) {
                            iniciarSesion(email, password) { cargando = it }
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Completa todos los campos",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(elevation = 4.dp, shape = MaterialTheme.shapes.medium),
                    enabled = !cargando,
                    shape = MaterialTheme.shapes.medium,
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
                        Text("INICIAR SESIÓN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón para ir a Registro
                TextButton(
                    onClick = {
                        startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                    },
                    enabled = !cargando
                ) {
                    Text(
                        "¿No tienes cuenta? Crear cuenta",
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    /**
     * ESTE ES EL COMPONENTE QUE DIBUJA EL ÍCONO
     * Un círculo suave con el emoji de construcción dentro.
     */
    @Composable
    fun IconoIncidente() {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp) // Tamaño del círculo
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), // Fondo suave
                    shape = CircleShape
                )
        ) {
            Text(
                text = "🚧", // Ícono simple de construcción/incidente
                fontSize = 60.sp // Tamaño del emoji
            )
        }
    }

    private fun iniciarSesion(email: String, password: String, onLoading: (Boolean) -> Unit) {
        onLoading(true)

        FirebaseConfig.getAuth().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onLoading(false)
                Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
            .addOnFailureListener { error ->
                onLoading(false)
                Toast.makeText(
                    this,
                    "Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}