package com.example.fixitcampus

import android.content.Context

// Guarda y lee la sesión del usuario en SharedPreferences
object Sesion {
    private const val FICHERO = "fixit_sesion"
    private const val CLAVE_ID = "usuario_id"
    private const val CLAVE_NOMBRE = "usuario_nombre"

    private fun prefs(c: Context) = c.getSharedPreferences(FICHERO, Context.MODE_PRIVATE)

    fun guardar(c: Context, id: Int, nombre: String) {
        prefs(c).edit().putInt(CLAVE_ID, id).putString(CLAVE_NOMBRE, nombre).apply()
    }

    fun id(c: Context): Int = prefs(c).getInt(CLAVE_ID, -1)

    fun nombre(c: Context): String = prefs(c).getString(CLAVE_NOMBRE, "") ?: ""

    fun cerrar(c: Context) {
        prefs(c).edit().clear().apply()
    }
}
