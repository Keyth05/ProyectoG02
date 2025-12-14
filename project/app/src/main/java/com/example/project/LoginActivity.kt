package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.project.ui.theme.ProjectTheme

/**
 * Pantalla de Login - Inicio de sesión simple
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
                // Título
                Text(
                    text = "Iniciar Sesión",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                // Campo de Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    enabled = !cargando
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
                    enabled = !cargando
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
                        .height(50.dp),
                    enabled = !cargando
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Entrar")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón para ir a Registro
                TextButton(
                    onClick = {
                        startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                    },
                    enabled = !cargando
                ) {
                    Text("¿No tienes cuenta? Regístrate")
                }
            }
        }
    }
    
    /**
     * Función simple para iniciar sesión con Firebase
     */
    private fun iniciarSesion(email: String, password: String, onLoading: (Boolean) -> Unit) {
        onLoading(true)
        
        FirebaseConfig.getAuth().signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onLoading(false)
                Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                // Ir al Dashboard
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
