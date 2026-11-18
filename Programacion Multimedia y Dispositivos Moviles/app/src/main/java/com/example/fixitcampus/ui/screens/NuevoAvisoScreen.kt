package com.example.fixitcampus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fixitcampus.data.RepositorioDatos

/** Formulario para crear un aviso nuevo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoAvisoScreen(
    onGuardar: (String, String, String, String) -> Unit,
    onAtras: () -> Unit
) {
    var espacio by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var prioridad by remember { mutableStateOf("Media") }
    var descripcion by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    var desplegarEspacios by remember { mutableStateOf(false) }
    var desplegarCategorias by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo aviso", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onAtras) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // Desplegable de espacios
            ExposedDropdownMenuBox(
                expanded = desplegarEspacios,
                onExpandedChange = { desplegarEspacios = !desplegarEspacios }
            ) {
                OutlinedTextField(
                    value = espacio,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Espacio *") },
                    placeholder = { Text("Selecciona...") },
                    isError = error && espacio.isBlank(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegarEspacios)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = desplegarEspacios,
                    onDismissRequest = { desplegarEspacios = false }
                ) {
                    RepositorioDatos.espacios.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                espacio = opcion
                                desplegarEspacios = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Desplegable de categorías
            ExposedDropdownMenuBox(
                expanded = desplegarCategorias,
                onExpandedChange = { desplegarCategorias = !desplegarCategorias }
            ) {
                OutlinedTextField(
                    value = categoria,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría *") },
                    placeholder = { Text("Selecciona...") },
                    isError = error && categoria.isBlank(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = desplegarCategorias)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = desplegarCategorias,
                    onDismissRequest = { desplegarCategorias = false }
                ) {
                    RepositorioDatos.categorias.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Text(opcion) },
                            onClick = {
                                categoria = opcion
                                desplegarCategorias = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Prioridad", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(6.dp))

            // Selección única de prioridad
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Baja", "Media", "Alta").forEach { opcion ->
                    FilterChip(
                        selected = prioridad == opcion,
                        onClick = { prioridad = opcion },
                        label = { Text(opcion) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción *") },
                placeholder = { Text("Describe la avería...") },
                minLines = 3,
                isError = error && descripcion.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            // Mensaje de validación
            if (error) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Completa los campos obligatorios (*)",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (espacio.isBlank() || categoria.isBlank() || descripcion.isBlank()) {
                        error = true
                    } else {
                        error = false
                        onGuardar(espacio, categoria, prioridad, descripcion.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C4DFF),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Enviar aviso", fontWeight = FontWeight.Bold)
            }
        }
    }
}
