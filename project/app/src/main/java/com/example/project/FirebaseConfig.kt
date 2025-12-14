package com.example.project

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

/**
 * Clase simple para configurar Firebase
 * Usa el archivo google-services.json automáticamente
 */
object FirebaseConfig {
    
    fun inicializar(app: android.app.Application) {
        try {
            // Firebase se inicializa automáticamente con google-services.json
            if (FirebaseApp.getApps(app).isEmpty()) {
                FirebaseApp.initializeApp(app)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Métodos simples para obtener las instancias
    fun getAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    fun getDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()
}
