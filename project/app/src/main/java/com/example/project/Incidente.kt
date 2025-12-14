package com.example.project

/**
 * Modelo de datos para un Incidente
 */
data class Incidente(
    val id: String = "",
    val descripcion: String = "",
    val fotoUrl: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fecha: Long = System.currentTimeMillis(),
    val usuarioEmail: String = "",
    val usuarioId: String = ""
) {
    // Constructor vacío necesario para Firebase
    constructor() : this("", "", "", 0.0, 0.0, 0L, "", "")
    
    /**
     * Convierte a Map para guardar en Firebase
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "descripcion" to descripcion,
            "fotoUrl" to fotoUrl,
            "latitud" to latitud,
            "longitud" to longitud,
            "fecha" to fecha,
            "usuarioEmail" to usuarioEmail,
            "usuarioId" to usuarioId
        )
    }
}
