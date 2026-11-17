package com.example.fixitcampus

import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File

// M4 - Detalle de un aviso con historial y comentarios
class DetalleAvisoActivity : AppCompatActivity() {

    private lateinit var db: DbHelper
    private lateinit var tvCodigo: TextView
    private lateinit var tvBadgeEstado: TextView
    private lateinit var tvUbicacion: TextView
    private lateinit var tvPrioridad: TextView
    private lateinit var vPunto: View
    private lateinit var ivFoto: ImageView
    private lateinit var tvDescripcion: TextView
    private lateinit var tvObjetivo: TextView
    private lateinit var layoutHistorial: LinearLayout
    private lateinit var layoutComentarios: LinearLayout
    private lateinit var etComentario: EditText
    private lateinit var btnEnviarComentario: ImageView

    private var avisoId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_aviso)

        avisoId = intent.getIntExtra("incidencia_id", -1)
        if (avisoId == -1) {
            finish()
            return
        }

        db = DbHelper(this)
        tvCodigo = findViewById(R.id.tvCodigo)
        tvBadgeEstado = findViewById(R.id.tvBadgeEstado)
        tvUbicacion = findViewById(R.id.tvUbicacion)
        tvPrioridad = findViewById(R.id.tvPrioridad)
        vPunto = findViewById(R.id.vPuntoPrioridad)
        ivFoto = findViewById(R.id.ivFoto)
        tvDescripcion = findViewById(R.id.tvDescripcion)
        tvObjetivo = findViewById(R.id.tvObjetivo)
        layoutHistorial = findViewById(R.id.layoutHistorial)
        layoutComentarios = findViewById(R.id.layoutComentarios)
        etComentario = findViewById(R.id.etComentario)
        btnEnviarComentario = findViewById(R.id.btnEnviarComentario)

        findViewById<ImageView>(R.id.ivAtras).setOnClickListener { finish() }
        btnEnviarComentario.setOnClickListener { enviarComentario() }

        cargar()
    }

    private fun cargar() {
        val aviso = db.obtenerAviso(avisoId)
        if (aviso == null) {
            finish()
            return
        }

        tvCodigo.text = aviso.codigo
        tvBadgeEstado.text = aviso.estado
        tvUbicacion.text = aviso.espacio
        tvPrioridad.text = aviso.prioridad

        // Colores según el estado
        if (aviso.estado == "RESUELTA") {
            tvBadgeEstado.setBackgroundResource(R.drawable.bg_badge_resuelta)
            tvBadgeEstado.setTextColor(ContextCompat.getColor(this, R.color.resuelta_texto))
            etComentario.isEnabled = false
            etComentario.hint = "Aviso resuelto"
            btnEnviarComentario.isEnabled = false
            btnEnviarComentario.alpha = 0.4f
        } else {
            tvBadgeEstado.setBackgroundResource(R.drawable.bg_badge_abierta)
            tvBadgeEstado.setTextColor(ContextCompat.getColor(this, R.color.texto))
        }

        // Punto de color de la prioridad
        val color = AvisoAdapter.colorPrioridad(this, aviso.prioridad)
        tvPrioridad.setTextColor(color)
        vPunto.backgroundTintList = ColorStateList.valueOf(color)

        // Foto guardada o imagen de ejemplo
        if (aviso.foto.isNotEmpty() && File(aviso.foto).exists()) {
            ivFoto.setImageBitmap(BitmapFactory.decodeFile(aviso.foto))
        } else {
            ivFoto.setImageResource(R.drawable.imagen_ejemplo)
        }

        tvDescripcion.text = aviso.descripcion
        if (aviso.fechaObjetivo.isNotEmpty()) {
            tvObjetivo.text = "Plazo objetivo: " + aviso.fechaObjetivo
        } else {
            tvObjetivo.visibility = View.GONE
        }

        cargarHistorial()
        cargarComentarios()
    }

    private fun cargarHistorial() {
        layoutHistorial.removeAllViews()
        val historial = db.listarHistorial(avisoId)
        for (item in historial) {
            val fila = LinearLayout(this)
            fila.orientation = LinearLayout.HORIZONTAL
            fila.gravity = android.view.Gravity.CENTER_VERTICAL
            fila.setPadding(0, dp(6), 0, dp(6))

            val punto = View(this)
            punto.layoutParams = LinearLayout.LayoutParams(dp(9), dp(9))
            punto.background = ContextCompat.getDrawable(this, R.drawable.bg_dot)
            val resuelta = item.titulo.lowercase().contains("resuelt")
            punto.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(this, if (resuelta) R.color.verde else R.color.morado)
            )

            val texto = TextView(this)
            texto.text = item.titulo + " - " + item.fecha
            texto.setTextColor(ContextCompat.getColor(this, R.color.secundario))
            texto.textSize = 13f
            texto.setPadding(dp(10), 0, 0, 0)

            fila.addView(punto)
            fila.addView(texto)
            layoutHistorial.addView(fila)
        }
    }

    private fun cargarComentarios() {
        layoutComentarios.removeAllViews()
        val comentarios = db.listarComentarios(avisoId)

        if (comentarios.isEmpty()) {
            val vacio = TextView(this)
            vacio.text = "Sin comentarios todavía."
            vacio.setTextColor(ContextCompat.getColor(this, R.color.secundario))
            vacio.textSize = 12f
            layoutComentarios.addView(vacio)
            return
        }

        for (comentario in comentarios) {
            val bloque = LinearLayout(this)
            bloque.orientation = LinearLayout.VERTICAL
            bloque.setPadding(0, dp(8), 0, dp(4))

            // Cabecera: avatar, autor y fecha
            val cabecera = LinearLayout(this)
            cabecera.orientation = LinearLayout.HORIZONTAL
            cabecera.gravity = android.view.Gravity.CENTER_VERTICAL

            val avatar = ImageView(this)
            avatar.setImageResource(R.drawable.ic_profile)
            avatar.background = ContextCompat.getDrawable(this, R.drawable.bg_avatar)
            avatar.setPadding(dp(5), dp(5), dp(5), dp(5))
            cabecera.addView(avatar, LinearLayout.LayoutParams(dp(28), dp(28)))

            val autor = TextView(this)
            autor.text = comentario.autor
            autor.setTextColor(ContextCompat.getColor(this, R.color.texto))
            autor.textSize = 13f
            autor.setTypeface(null, android.graphics.Typeface.BOLD)
            val paramsAutor = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            paramsAutor.marginStart = dp(8)
            cabecera.addView(autor, paramsAutor)

            val fecha = TextView(this)
            fecha.text = comentario.fecha
            fecha.setTextColor(ContextCompat.getColor(this, R.color.secundario))
            fecha.textSize = 11f
            cabecera.addView(fecha)

            val texto = TextView(this)
            texto.text = comentario.texto
            texto.setTextColor(ContextCompat.getColor(this, R.color.secundario))
            texto.textSize = 13f
            texto.setPadding(dp(36), dp(4), 0, 0)

            bloque.addView(cabecera)
            bloque.addView(texto)
            layoutComentarios.addView(bloque)
        }
    }

    private fun enviarComentario() {
        val texto = etComentario.text.toString().trim()
        if (texto.isEmpty()) return

        db.insertarComentario(avisoId, "Tú", texto, DbHelper.fechaHoy())
        etComentario.setText("")
        cargarComentarios()

        val teclado = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        teclado.hideSoftInputFromWindow(etComentario.windowToken, 0)
    }

    private fun dp(valor: Int): Int = (valor * resources.displayMetrics.density).toInt()
}
