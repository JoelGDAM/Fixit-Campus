package com.example.fixitcampus

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import java.io.File

// Formulario para crear un aviso
class NuevoAvisoActivity : AppCompatActivity() {

    private lateinit var db: DbHelper
    private lateinit var spEdificio: Spinner
    private lateinit var spEspacio: Spinner
    private lateinit var spCategoria: Spinner
    private lateinit var etDescripcion: EditText
    private lateinit var tvError: LinearLayout
    private lateinit var mcvFotoPreview: MaterialCardView
    private lateinit var ivFotoPreview: ImageView
    private lateinit var swRiesgo: SwitchMaterial
    private lateinit var swImpide: SwitchMaterial

    private var edificios: List<Pair<Int, String>> = listOf()
    private var espacios: List<Pair<Int, String>> = listOf()
    private var categorias: List<Pair<Int, String>> = listOf()

    private var idEdificio = 0
    private var idEspacio = 0
    private var idCategoria = 0
    private var fotoRuta = ""
    private var fotoUri: Uri? = null
    private var archivoCamara: File? = null

    companion object {
        private const val RC_CAMARA = 10
        private const val RC_GALERIA = 20
        private const val RC_PERMISO = 30
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nuevo_aviso)

        db = DbHelper(this)
        spEdificio = findViewById(R.id.spEdificio)
        spEspacio = findViewById(R.id.spEspacio)
        spCategoria = findViewById(R.id.spCategoria)
        etDescripcion = findViewById(R.id.etDescripcion)
        tvError = findViewById(R.id.tvError)
        mcvFotoPreview = findViewById(R.id.mcvFotoPreview)
        ivFotoPreview = findViewById(R.id.ivFotoPreview)
        swRiesgo = findViewById(R.id.swRiesgo)
        swImpide = findViewById(R.id.swImpide)

        findViewById<ImageView>(R.id.ivAtras).setOnClickListener { finish() }

        // Categorías fijas
        categorias = db.listarCategorias()
        spCategoria.adapter = adaptador("Selecciona una categoría", categorias)
        spCategoria.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                idCategoria = if (position > 0) categorias[position - 1].first else 0
                if (position > 0) spCategoria.setBackgroundResource(R.drawable.bg_input)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Edificios y espacios dependientes
        edificios = db.listarEdificios()
        spEdificio.adapter = adaptador("Selecciona un edificio", edificios)
        spEdificio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                idEdificio = if (position > 0) edificios[position - 1].first else 0
                if (position > 0) spEdificio.setBackgroundResource(R.drawable.bg_input)
                cargarEspacios()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spEspacio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                idEspacio = if (position > 0) espacios[position - 1].first else 0
                if (position > 0) spEspacio.setBackgroundResource(R.drawable.bg_input)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // El espacio empieza vacío hasta elegir un edificio
        cargarEspacios()

        // Al escribir, quitamos el borde rojo de error
        etDescripcion.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (!s.isNullOrBlank()) etDescripcion.setBackgroundResource(R.drawable.bg_input)
            }
        })

        findViewById<Button>(R.id.btnFoto).setOnClickListener { dialogoFoto() }
        findViewById<Button>(R.id.btnEnviar).setOnClickListener { enviar() }
    }

    private fun adaptador(vacio: String, datos: List<Pair<Int, String>>): ArrayAdapter<String> {
        val opciones = mutableListOf(vacio)
        for (d in datos) opciones.add(d.second)
        val adaptador = ArrayAdapter(this, R.layout.spinner_item, opciones)
        adaptador.setDropDownViewResource(R.layout.spinner_dropdown_item)
        return adaptador
    }

    private fun cargarEspacios() {
        espacios = if (idEdificio > 0) db.listarEspacios(idEdificio) else listOf()
        spEspacio.adapter = adaptador("Selecciona un espacio", espacios)
        spEspacio.setSelection(0)
        idEspacio = 0
    }

    // Foto
    private fun dialogoFoto() {
        val opciones = arrayOf("Hacer foto", "Subir de galería")
        AlertDialog.Builder(this, R.style.DialogoFoto)
            .setTitle("Añadir una foto")
            .setItems(opciones) { _, cual ->
                if (cual == 0) pedirCamara() else abrirGaleria()
            }
            .show()
    }

    private fun pedirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), RC_PERMISO)
        }
    }

    private fun abrirCamara() {
        val carpeta = File(filesDir, "fotos")
        if (!carpeta.exists()) carpeta.mkdirs()
        val archivo = File(carpeta, "tmp_" + System.currentTimeMillis() + ".jpg")
        archivoCamara = archivo
        try {
            archivo.createNewFile()
        } catch (e: Exception) {
        }
        fotoUri = FileProvider.getUriForFile(this, packageName + ".fileprovider", archivo)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri)
        // Permiso temporal para que la cámara escriba en nuestro archivo
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivityForResult(intent, RC_CAMARA)
        } catch (e: Exception) {
            Toast.makeText(this, "No hay ninguna cámara disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, RC_GALERIA)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_PERMISO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return

        val uri: Uri? = if (requestCode == RC_CAMARA) fotoUri else data?.data
        if (uri == null) return

        val ruta = ImagenUtils.comprimirYGuardar(this, uri)
        if (requestCode == RC_CAMARA) archivoCamara?.delete()

        if (ruta == null) {
            Toast.makeText(this, "No se ha podido guardar la foto", Toast.LENGTH_SHORT).show()
            return
        }
        fotoRuta = ruta
        ivFotoPreview.setImageBitmap(BitmapFactory.decodeFile(ruta))
        mcvFotoPreview.visibility = View.VISIBLE
    }

    // Guardar aviso
    private fun enviar() {
        val descripcion = etDescripcion.text.toString().trim()
        var correcto = true

        if (idEdificio == 0) {
            spEdificio.setBackgroundResource(R.drawable.bg_input_error)
            correcto = false
        } else {
            spEdificio.setBackgroundResource(R.drawable.bg_input)
        }

        if (idEspacio == 0) {
            spEspacio.setBackgroundResource(R.drawable.bg_input_error)
            correcto = false
        } else {
            spEspacio.setBackgroundResource(R.drawable.bg_input)
        }

        if (idCategoria == 0) {
            spCategoria.setBackgroundResource(R.drawable.bg_input_error)
            correcto = false
        } else {
            spCategoria.setBackgroundResource(R.drawable.bg_input)
        }

        if (descripcion.isEmpty()) {
            etDescripcion.setBackgroundResource(R.drawable.bg_input_error)
            correcto = false
        } else {
            etDescripcion.setBackgroundResource(R.drawable.bg_input)
        }

        tvError.visibility = if (correcto) View.GONE else View.VISIBLE
        if (!correcto) return

        // Prioridad y plazo según los switches
        val prioridad: String
        val objetivo: String
        when {
            swRiesgo.isChecked -> {
                prioridad = "Urgente"
                objetivo = "Hoy"
            }
            swImpide.isChecked -> {
                prioridad = "Alta"
                objetivo = DbHelper.sumarDiasLaborables(1)
            }
            else -> {
                prioridad = "Media"
                objetivo = DbHelper.sumarDiasLaborables(3)
            }
        }

        val usuarioId = Sesion.id(this)
        if (usuarioId == -1) {
            finish()
            return
        }

        val id = db.insertarIncidencia(usuarioId, idEspacio, idCategoria, descripcion, fotoRuta, prioridad, objetivo)
        mostrarConfirmacion(id)
    }

    // Pantalla de confirmación
    private fun mostrarConfirmacion(id: Int) {
        setContentView(R.layout.activity_confirmacion)

        val codigo = db.obtenerCodigo(id)
        findViewById<android.widget.TextView>(R.id.tvMensaje).text =
            "Hemos registrado el aviso " + codigo + ". Te avisaremos cuando cambie su estado."

        findViewById<Button>(R.id.btnVerAviso).setOnClickListener {
            val intent = Intent(this, DetalleAvisoActivity::class.java)
            intent.putExtra("incidencia_id", id)
            startActivity(intent)
            finish()
        }
        findViewById<Button>(R.id.btnVolver).setOnClickListener { finish() }
    }
}
