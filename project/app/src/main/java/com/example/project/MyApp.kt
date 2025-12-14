package com.example.project

import android.app.Application

class MyApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Inicializar Firebase cuando la app arranca
        FirebaseConfig.inicializar(this)
    }
}
