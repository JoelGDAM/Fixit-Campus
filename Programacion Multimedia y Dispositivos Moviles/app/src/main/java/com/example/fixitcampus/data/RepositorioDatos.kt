package com.example.fixitcampus.data

import androidx.compose.runtime.mutableStateListOf
import com.example.fixitcampus.model.Incidencia

// Repositorio en memoria (sin Android), compartido por toda la app
object RepositorioDatos {

    val espacios: List<String> = listOf(
        "Aula 101", "Aula 102", "Aula 103", "Aula 104",
        "Biblioteca", "Laboratorio A", "Laboratorio 1",
        "Laboratorio 2", "Pasillo", "Baño"
    )

    val categorias: List<String> = listOf(
        "Electricidad", "Fontanería", "Informática", "Mobiliario"
    )

    // Lista observable de incidencias
    private val _incidencias = mutableStateListOf(
        Incidencia("FIX-001", "2026-10-21", "Biblioteca", "Electricidad", "Media", "RESUELTA",
            "Una luz parpadea constantemente"),
        Incidencia("FIX-002", "2026-10-20", "Laboratorio A", "Mobiliario", "Baja", "EN CURSO",
            "Una silla está rota"),
        Incidencia("FIX-003", "2026-10-20", "Aula 101", "Informática", "Alta", "ABIERTA",
            "El proyector se enciende pero no muestra imagen"),
        Incidencia("FIX-004", "2026-10-21", "Aula 102", "Fontanería", "Baja", "ABIERTA",
            "El grifo gotea"),
        Incidencia("FIX-005", "2026-10-22", "Pasillo", "Electricidad", "Alta", "EN CURSO",
            "Una luz del pasillo no enciende")
    )

    val incidencias: List<Incidencia> get() = _incidencias

    // Login sencillo del sprint 1
    fun validarLogin(email: String, pass: String): Boolean =
        email.trim().equals("alumno@campus.es", true) && pass == "1234"

    // Crea una incidencia nueva y la añade al final
    fun agregarIncidencia(
        espacio: String,
        categoria: String,
        prioridad: String,
        descripcion: String
    ): Incidencia {
        // Siguiente código = mayor número existente + 1
        val maxNumero = _incidencias
            .mapNotNull { it.codigo.removePrefix("FIX-").toIntOrNull() }
            .maxOrNull() ?: 0
        val codigo = "FIX-" + (maxNumero + 1).toString().padStart(3, '0')

        val nueva = Incidencia(
            codigo = codigo,
            fecha = "2026-10-23",
            espacio = espacio,
            categoria = categoria,
            prioridad = prioridad,
            estado = "ABIERTA",
            descripcion = descripcion
        )
        _incidencias.add(nueva)
        return nueva
    }

    fun buscarPorCodigo(codigo: String): Incidencia? =
        _incidencias.firstOrNull { it.codigo == codigo }

    fun filtrarPorEstado(estado: String): List<Incidencia> =
        if (estado == "TODAS") _incidencias.toList()
        else _incidencias.filter { it.estado == estado }
}
