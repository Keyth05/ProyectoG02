package com.example.project

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * MainActivity - Punto de entrada de la aplicación
 * Verifica si el usuario ya inició sesión y lo redirige
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Verificar si el usuario ya está logueado
        val usuarioActual = FirebaseConfig.getAuth().currentUser
        
        if (usuarioActual != null) {
            // Ya está logueado, ir al Dashboard
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        } else {
            // No está logueado, ir al Login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}