package com.example.fixitcampus.model

// Modelo de una incidencia del campus
data class Incidencia(
    val codigo: String,
    val fecha: String,
    val espacio: String,
    val categoria: String,
    val prioridad: String,
    val estado: String,
    val descripcion: String
)
