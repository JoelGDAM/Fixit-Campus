package com.example.fixitcampus

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest
import java.util.Calendar
import java.util.Date

// Usuario devuelto por el login
data class Usuario(val id: Int, val nombre: String, val email: String)

// Acceso a la base de datos SQLite (fixit.db)
class DbHelper(context: Context) : SQLiteOpenHelper(context, "fixit.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, email TEXT UNIQUE, salt TEXT, hash TEXT)")
        db.execSQL("CREATE TABLE edificios (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT)")
        db.execSQL("CREATE TABLE espacios (id INTEGER PRIMARY KEY AUTOINCREMENT, edificio_id INTEGER, nombre TEXT)")
        db.execSQL("CREATE TABLE categorias (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT)")
        db.execSQL("CREATE TABLE incidencias (id INTEGER PRIMARY KEY AUTOINCREMENT, codigo TEXT UNIQUE, usuario_id INTEGER, espacio_id INTEGER, categoria_id INTEGER, descripcion TEXT, foto TEXT, prioridad TEXT, estado TEXT, fecha_creacion TEXT, fecha_objetivo TEXT)")
        db.execSQL("CREATE TABLE historial (id INTEGER PRIMARY KEY AUTOINCREMENT, incidencia_id INTEGER, titulo TEXT, fecha TEXT)")
        db.execSQL("CREATE TABLE comentarios (id INTEGER PRIMARY KEY AUTOINCREMENT, incidencia_id INTEGER, autor TEXT, texto TEXT, fecha TEXT)")
        sembrar(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        db.execSQL("DROP TABLE IF EXISTS edificios")
        db.execSQL("DROP TABLE IF EXISTS espacios")
        db.execSQL("DROP TABLE IF EXISTS categorias")
        db.execSQL("DROP TABLE IF EXISTS incidencias")
        db.execSQL("DROP TABLE IF EXISTS historial")
        db.execSQL("DROP TABLE IF EXISTS comentarios")
        onCreate(db)
    }

    // ---------- Hash SHA-256 con salt ----------
    fun crearHash(pass: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((salt + pass).toByteArray())
        val sb = StringBuilder()
        for (b in bytes) sb.append(String.format("%02x", b.toInt() and 0xff))
        return sb.toString()
    }

    fun login(email: String, pass: String): Usuario? {
        val c = readableDatabase.rawQuery(
            "SELECT id, nombre, salt, hash FROM usuarios WHERE email = ?",
            arrayOf(email.trim().lowercase())
        )
        var usuario: Usuario? = null
        if (c.moveToFirst() && crearHash(pass, c.getString(2)) == c.getString(3)) {
            usuario = Usuario(c.getInt(0), c.getString(1), email.trim())
        }
        c.close()
        return usuario
    }

    // ---------- Datos para los spinners ----------
    fun listarEdificios(): List<Pair<Int, String>> = listaSimple("SELECT id, nombre FROM edificios ORDER BY id")

    fun listarEspacios(edificioId: Int): List<Pair<Int, String>> =
        listaSimple("SELECT id, nombre FROM espacios WHERE edificio_id = $edificioId ORDER BY id")

    fun listarCategorias(): List<Pair<Int, String>> = listaSimple("SELECT id, nombre FROM categorias ORDER BY id")

    private fun listaSimple(sql: String): List<Pair<Int, String>> {
        val lista = mutableListOf<Pair<Int, String>>()
        val c = readableDatabase.rawQuery(sql, null)
        while (c.moveToNext()) lista.add(Pair(c.getInt(0), c.getString(1)))
        c.close()
        return lista
    }

    // ---------- Avisos ----------
    fun listarIncidencias(usuarioId: Int, filtro: String, texto: String): List<Aviso> {
        val lista = mutableListOf<Aviso>()
        var sql = "SELECT i.id, i.codigo, e.nombre, c.nombre, i.prioridad, i.estado, i.descripcion, " +
                "i.fecha_creacion, i.fecha_objetivo, i.foto FROM incidencias i " +
                "JOIN espacios e ON e.id = i.espacio_id JOIN categorias c ON c.id = i.categoria_id " +
                "WHERE i.usuario_id = ?"
        val args = mutableListOf(usuarioId.toString())
        if (filtro != "TODAS") {
            sql += " AND i.estado = ?"
            args.add(filtro)
        }
        if (texto.isNotEmpty()) {
            sql += " AND (i.codigo LIKE ? OR i.descripcion LIKE ? OR e.nombre LIKE ?)"
            val like = "%$texto%"
            args.add(like)
            args.add(like)
            args.add(like)
        }
        sql += " ORDER BY i.id DESC"
        val c = readableDatabase.rawQuery(sql, args.toTypedArray())
        while (c.moveToNext()) lista.add(leerAviso(c))
        c.close()
        return lista
    }

    fun obtenerAviso(id: Int): Aviso? {
        val c = readableDatabase.rawQuery(
            "SELECT i.id, i.codigo, e.nombre, c.nombre, i.prioridad, i.estado, i.descripcion, " +
                    "i.fecha_creacion, i.fecha_objetivo, i.foto FROM incidencias i " +
                    "JOIN espacios e ON e.id = i.espacio_id JOIN categorias c ON c.id = i.categoria_id " +
                    "WHERE i.id = ?", arrayOf(id.toString())
        )
        val aviso = if (c.moveToFirst()) leerAviso(c) else null
        c.close()
        return aviso
    }

    private fun leerAviso(c: Cursor) = Aviso(
        c.getInt(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4),
        c.getString(5), c.getString(6), c.getString(7), c.getString(8), c.getString(9)
    )

    fun insertarIncidencia(
        usuarioId: Int, espacioId: Int, categoriaId: Int,
        descripcion: String, foto: String, prioridad: String, fechaObjetivo: String
    ): Int {
        val db = writableDatabase
        val codigo = "FIX-" + String.format("%03d", siguienteId(db))
        val fecha = formatoFecha(Date())
        val v = ContentValues()
        v.put("codigo", codigo)
        v.put("usuario_id", usuarioId)
        v.put("espacio_id", espacioId)
        v.put("categoria_id", categoriaId)
        v.put("descripcion", descripcion)
        v.put("foto", foto)
        v.put("prioridad", prioridad)
        v.put("estado", "ABIERTA")
        v.put("fecha_creacion", fecha)
        v.put("fecha_objetivo", fechaObjetivo)
        val id = db.insert("incidencias", null, v).toInt()
        insertarHistorial(id, "Aviso creado", fecha)
        return id
    }

    private fun siguienteId(db: SQLiteDatabase): Int {
        val c = db.rawQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM incidencias", null)
        val siguiente = if (c.moveToFirst()) c.getInt(0) else 1
        c.close()
        return siguiente
    }

    fun obtenerCodigo(id: Int): String {
        val c = readableDatabase.rawQuery("SELECT codigo FROM incidencias WHERE id = ?", arrayOf(id.toString()))
        val codigo = if (c.moveToFirst()) c.getString(0) else ""
        c.close()
        return codigo
    }

    // ---------- Historial y comentarios ----------
    fun listarHistorial(incidenciaId: Int): List<HistorialItem> {
        val lista = mutableListOf<HistorialItem>()
        val c = readableDatabase.rawQuery(
            "SELECT id, titulo, fecha FROM historial WHERE incidencia_id = ? ORDER BY id",
            arrayOf(incidenciaId.toString())
        )
        while (c.moveToNext()) lista.add(HistorialItem(c.getInt(0), c.getString(1), c.getString(2)))
        c.close()
        return lista
    }

    private fun insertarHistorial(incidenciaId: Int, titulo: String, fecha: String) {
        val v = ContentValues()
        v.put("incidencia_id", incidenciaId)
        v.put("titulo", titulo)
        v.put("fecha", fecha)
        writableDatabase.insert("historial", null, v)
    }

    fun listarComentarios(incidenciaId: Int): List<Comentario> {
        val lista = mutableListOf<Comentario>()
        val c = readableDatabase.rawQuery(
            "SELECT id, autor, texto, fecha FROM comentarios WHERE incidencia_id = ? ORDER BY id",
            arrayOf(incidenciaId.toString())
        )
        while (c.moveToNext()) lista.add(Comentario(c.getInt(0), c.getString(1), c.getString(2), c.getString(3)))
        c.close()
        return lista
    }

    fun insertarComentario(incidenciaId: Int, autor: String, texto: String, fecha: String) {
        val v = ContentValues()
        v.put("incidencia_id", incidenciaId)
        v.put("autor", autor)
        v.put("texto", texto)
        v.put("fecha", fecha)
        writableDatabase.insert("comentarios", null, v)
    }

    // ---------- Datos precargados ----------
    private fun sembrar(db: SQLiteDatabase) {
        insertarUsuario(db, 1, "Alumno Campus", "alumno@campus.es", "s4l4l")
        insertarUsuario(db, 2, "María López", "maria@campus.es", "m4r14")

        insertarFila(db, "edificios", 1, "Edificio A")
        insertarFila(db, "edificios", 2, "Edificio B")

        val espacios = listOf(
            Pair(1, "Aula 101"), Pair(1, "Aula 102"), Pair(1, "Aula 103"),
            Pair(1, "Aula 105"), Pair(1, "Laboratorio A"),
            Pair(2, "Laboratorio B"), Pair(2, "Laboratorio C"), Pair(2, "Taller 1"),
            Pair(2, "Taller 2"), Pair(2, "Aula 201")
        )
        for ((i, datos) in espacios.withIndex()) {
            val v = ContentValues()
            v.put("id", i + 1)
            v.put("edificio_id", datos.first)
            v.put("nombre", datos.second)
            db.insert("espacios", null, v)
        }

        val categorias = listOf("Electricidad", "Fontanería", "Climatización", "Informática", "Mobiliario")
        for ((i, nombre) in categorias.withIndex()) insertarFila(db, "categorias", i + 1, nombre)

        val hace15min = formatoFecha(Date(System.currentTimeMillis() - 15 * 60 * 1000))

        // FIX-001 (resuelta)
        insertarAvisoSeed(db, 1, "FIX-001", 1, 4, 4, "El proyector del aula no enciende.", "Alta", "RESUELTA", "12 nov, 09:15", "13 nov")
        insertarHistorialSeed(db, 1, "Aviso creado", "12 nov, 09:15")
        insertarHistorialSeed(db, 1, "Resuelta", "13 nov, 11:40")

        // FIX-002
        insertarAvisoSeed(db, 2, "FIX-002", 1, 5, 5, "Silla rota en la tercera fila.", "Media", "ABIERTA", "10 nov, 11:40", "13 nov")
        insertarHistorialSeed(db, 2, "Aviso creado", "10 nov, 11:40")

        // FIX-003 (con historial y comentario del técnico)
        insertarAvisoSeed(db, 3, "FIX-003", 1, 1, 4, "El proyector no muestra imagen desde esta mañana.", "Alta", "ABIERTA", "12 nov, 09:15", "13 nov")
        insertarHistorialSeed(db, 3, "Aviso creado", "12 nov, 09:15")
        insertarHistorialSeed(db, 3, "En revisión por el técnico", "13 nov, 09:30")
        val comentario = ContentValues()
        comentario.put("incidencia_id", 3)
        comentario.put("autor", "Técnico 1")
        comentario.put("texto", "Estamos revisando el equipo.")
        comentario.put("fecha", "Hoy, 10:30")
        db.insert("comentarios", null, comentario)

        // FIX-004 (el más reciente)
        insertarAvisoSeed(db, 4, "FIX-004", 1, 2, 2, "Gotera de grifo en el techo del aula.", "Baja", "ABIERTA", hace15min, "Hoy")
        insertarHistorialSeed(db, 4, "Aviso creado", hace15min)
    }

    private fun insertarUsuario(db: SQLiteDatabase, id: Int, nombre: String, email: String, salt: String) {
        val v = ContentValues()
        v.put("id", id)
        v.put("nombre", nombre)
        v.put("email", email)
        v.put("salt", salt)
        v.put("hash", crearHash("1234", salt))
        db.insert("usuarios", null, v)
    }

    private fun insertarFila(db: SQLiteDatabase, tabla: String, id: Int, nombre: String) {
        val v = ContentValues()
        v.put("id", id)
        v.put("nombre", nombre)
        db.insert(tabla, null, v)
    }

    private fun insertarAvisoSeed(
        db: SQLiteDatabase, id: Int, codigo: String, usuarioId: Int, espacioId: Int, categoriaId: Int,
        descripcion: String, prioridad: String, estado: String, fecha: String, objetivo: String
    ) {
        val v = ContentValues()
        v.put("id", id)
        v.put("codigo", codigo)
        v.put("usuario_id", usuarioId)
        v.put("espacio_id", espacioId)
        v.put("categoria_id", categoriaId)
        v.put("descripcion", descripcion)
        v.put("foto", "")
        v.put("prioridad", prioridad)
        v.put("estado", estado)
        v.put("fecha_creacion", fecha)
        v.put("fecha_objetivo", objetivo)
        db.insert("incidencias", null, v)
    }

    private fun insertarHistorialSeed(db: SQLiteDatabase, incidenciaId: Int, titulo: String, fecha: String) {
        val v = ContentValues()
        v.put("incidencia_id", incidenciaId)
        v.put("titulo", titulo)
        v.put("fecha", fecha)
        db.insert("historial", null, v)
    }

    companion object {
        private val MESES = arrayOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sept", "oct", "nov", "dic")

        private fun diaMes(c: Calendar): String =
            c.get(Calendar.DAY_OF_MONTH).toString() + " " + MESES[c.get(Calendar.MONTH)]

        // Fecha legible: "12 nov, 09:15"
        fun formatoFecha(d: Date): String {
            val c = Calendar.getInstance()
            c.time = d
            return diaMes(c) + ", " + String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
        }

        fun fechaHoy(): String {
            val c = Calendar.getInstance()
            return "Hoy, " + String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
        }

        // Suma días laborables saltando sábado y domingo
        fun sumarDiasLaborables(dias: Int): String {
            val c = Calendar.getInstance()
            var contador = 0
            while (contador < dias) {
                c.add(Calendar.DAY_OF_MONTH, 1)
                val dia = c.get(Calendar.DAY_OF_WEEK)
                if (dia != Calendar.SATURDAY && dia != Calendar.SUNDAY) contador++
            }
            return diaMes(c)
        }

        // Convierte "12 nov, 09:15" en "15 min" o "12 nov"
        fun fechaRelativa(texto: String): String {
            try {
                val partes = texto.split(",")
                if (partes.size != 2) return texto
                val diaYMes = partes[0].trim().split(" ")
                if (diaYMes.size != 2) return texto
                val dia = diaYMes[0].toInt()
                val mes = MESES.indexOf(diaYMes[1].lowercase())
                if (mes < 0) return texto
                val hora = partes[1].trim().split(":")
                if (hora.size != 2) return texto

                val c = Calendar.getInstance()
                c.set(Calendar.DAY_OF_MONTH, 1)
                c.set(Calendar.MONTH, mes)
                c.set(Calendar.DAY_OF_MONTH, dia)
                c.set(Calendar.HOUR_OF_DAY, hora[0].toInt())
                c.set(Calendar.MINUTE, hora[1].toInt())
                c.set(Calendar.SECOND, 0)
                c.set(Calendar.MILLISECOND, 0)

                var minutos = (System.currentTimeMillis() - c.timeInMillis) / 60000
                if (minutos < 0) {
                    c.add(Calendar.YEAR, -1)
                    minutos = (System.currentTimeMillis() - c.timeInMillis) / 60000
                }
                if (minutos in 0..59) return "$minutos min"
                return partes[0].trim()
            } catch (e: Exception) {
                return texto
            }
        }
    }
}
