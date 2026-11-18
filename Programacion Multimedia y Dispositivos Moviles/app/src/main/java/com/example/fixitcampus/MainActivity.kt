package com.example.fixitcampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.fixitcampus.data.RepositorioDatos
import com.example.fixitcampus.ui.screens.DetalleAvisoScreen
import com.example.fixitcampus.ui.screens.LoginScreen
import com.example.fixitcampus.ui.screens.MisAvisosScreen
import com.example.fixitcampus.ui.screens.NuevoAvisoScreen
import com.example.fixitcampus.ui.theme.FixItTheme

/**
 * Única Activity de la app. La navegación se hace con un estado simple.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FixItTheme {
                // Pantalla actual y código seleccionado para el detalle
                var pantalla by remember { mutableStateOf("login") }
                var codigoSeleccionado by remember { mutableStateOf("") }

                // Botón atrás: en login cierra la app, en el resto vuelve a la lista
                BackHandler {
                    if (pantalla == "login") {
                        finish()
                    } else {
                        pantalla = "lista"
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (pantalla) {
                        "login" -> LoginScreen(
                            onLoginCorrecto = { pantalla = "lista" }
                        )

                        "lista" -> MisAvisosScreen(
                            onNuevo = { pantalla = "nuevo" },
                            onDetalle = { codigo ->
                                codigoSeleccionado = codigo
                                pantalla = "detalle"
                            },
                            onCerrarSesion = {
                                codigoSeleccionado = ""
                                pantalla = "login"
                            }
                        )

                        "nuevo" -> NuevoAvisoScreen(
                            onGuardar = { espacio, categoria, prioridad, descripcion ->
                                RepositorioDatos.agregarIncidencia(
                                    espacio, categoria, prioridad, descripcion
                                )
                                pantalla = "lista"
                            },
                            onAtras = { pantalla = "lista" }
                        )

                        "detalle" -> DetalleAvisoScreen(
                            incidencia = RepositorioDatos.buscarPorCodigo(codigoSeleccionado),
                            onAtras = { pantalla = "lista" }
                        )
                    }
                }
            }
        }
    }
}
