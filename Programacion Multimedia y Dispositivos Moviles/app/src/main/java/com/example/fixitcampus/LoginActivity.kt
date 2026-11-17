package com.example.fixitcampus

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// Pantalla de inicio de sesión
class LoginActivity : AppCompatActivity() {

    private lateinit var db: DbHelper
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvError: LinearLayout
    private var mostrarPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = DbHelper(this)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tvError = findViewById(R.id.tvError)

        // Mostrar y ocultar la contraseña
        findViewById<ImageView>(R.id.ivOjo).setOnClickListener {
            mostrarPassword = !mostrarPassword
            if (mostrarPassword) {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            etPassword.setSelection(etPassword.text.length)
        }

        findViewById<Button>(R.id.btnEntrar).setOnClickListener { entrar() }

        findViewById<TextView>(R.id.tvOlvido).setOnClickListener {
            Toast.makeText(this, "Contacta con conserjería para restablecer tu contraseña", Toast.LENGTH_SHORT).show()
        }
    }

    private fun entrar() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            tvError.visibility = View.VISIBLE
            return
        }

        val usuario = db.login(email, password)
        if (usuario == null) {
            tvError.visibility = View.VISIBLE
            etPassword.setText("")
            return
        }

        // Sesión guardada en SharedPreferences
        Sesion.guardar(this, usuario.id, usuario.nombre)
        ocultarTeclado()
        startActivity(Intent(this, MisAvisosActivity::class.java))
        finish()
    }

    private fun ocultarTeclado() {
        val teclado = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        teclado.hideSoftInputFromWindow(etPassword.windowToken, 0)
    }
}
