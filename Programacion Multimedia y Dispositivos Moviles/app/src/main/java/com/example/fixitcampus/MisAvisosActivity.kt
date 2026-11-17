package com.example.fixitcampus

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

// Listado de avisos del usuario
class MisAvisosActivity : AppCompatActivity() {

    private lateinit var db: DbHelper
    private lateinit var adapter: AvisoAdapter
    private lateinit var rvAvisos: RecyclerView
    private lateinit var layoutVacio: LinearLayout
    private lateinit var etBuscar: EditText
    private val chips = mutableListOf<TextView>()
    private var usuarioId = -1
    private var filtro = "TODAS"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_avisos)

        usuarioId = Sesion.id(this)
        if (usuarioId == -1) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        db = DbHelper(this)
        findViewById<TextView>(R.id.tvSaludo).text = Sesion.nombre(this)

        rvAvisos = findViewById(R.id.rvAvisos)
        layoutVacio = findViewById(R.id.layoutVacio)
        etBuscar = findViewById(R.id.etBuscar)

        rvAvisos.layoutManager = LinearLayoutManager(this)
        adapter = AvisoAdapter(listOf()) { aviso -> abrirDetalle(aviso.id) }
        rvAvisos.adapter = adapter

        chips.add(findViewById(R.id.chipTodas))
        chips.add(findViewById(R.id.chipAbiertas))
        chips.add(findViewById(R.id.chipResueltas))
        for (chip in chips) chip.setOnClickListener { seleccionarFiltro(chip) }

        findViewById<ImageView>(R.id.ivAjustes).setOnClickListener { cerrarSesion() }
        findViewById<ImageView>(R.id.ivFiltro).setOnClickListener {
            Toast.makeText(this, "Busca por código, descripción o espacio", Toast.LENGTH_SHORT).show()
        }
        findViewById<FloatingActionButton>(R.id.fabNuevo).setOnClickListener { abrirNuevo() }
        findViewById<Button>(R.id.btnNuevoVacio).setOnClickListener { abrirNuevo() }

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                cargar()
            }
        })

        seleccionarFiltro(chips[0])
    }

    override fun onResume() {
        super.onResume()
        if (usuarioId != -1) cargar()
    }

    private fun seleccionarFiltro(chip: TextView) {
        filtro = when (chip.id) {
            R.id.chipAbiertas -> "ABIERTA"
            R.id.chipResueltas -> "RESUELTA"
            else -> "TODAS"
        }
        for (c in chips) {
            val activo = c == chip
            c.setBackgroundResource(if (activo) R.drawable.bg_chip_active else R.drawable.bg_chip_inactive)
            c.setTextColor(ContextCompat.getColor(this, if (activo) R.color.texto else R.color.secundario))
        }
        cargar()
    }

    private fun cargar() {
        val lista = db.listarIncidencias(usuarioId, filtro, etBuscar.text.toString().trim())
        adapter.actualizar(lista)
        val vacio = lista.isEmpty()
        layoutVacio.visibility = if (vacio) View.VISIBLE else View.GONE
        rvAvisos.visibility = if (vacio) View.GONE else View.VISIBLE
    }

    private fun abrirNuevo() {
        startActivity(Intent(this, NuevoAvisoActivity::class.java))
    }

    private fun abrirDetalle(id: Int) {
        val intent = Intent(this, DetalleAvisoActivity::class.java)
        intent.putExtra("incidencia_id", id)
        startActivity(intent)
    }

    private fun cerrarSesion() {
        Sesion.cerrar(this)
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
