package com.example.fixitcampus

// Datos de un aviso tal y como se muestran en las pantallas
data class Aviso(
    val id: Int,
    val codigo: String,
    val espacio: String,
    val categoria: String,
    val prioridad: String,
    val estado: String,
    val descripcion: String,
    val fecha: String,
    val fechaObjetivo: String,
    val foto: String
)
