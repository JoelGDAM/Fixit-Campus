package com.example.fixitcampus

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

// Comprime la foto elegida (máx. 1024 px y JPEG 75%) y la guarda en filesDir/fotos
object ImagenUtils {

    fun comprimirYGuardar(context: Context, uri: Uri): String? {
        try {
            // Primero medimos la imagen para no cargar una foto enorme en memoria
            val medidas = BitmapFactory.Options()
            medidas.inJustDecodeBounds = true
            var entrada = context.contentResolver.openInputStream(uri) ?: return null
            BitmapFactory.decodeStream(entrada, null, medidas)
            entrada.close()

            var muestra = 1
            while (medidas.outWidth / muestra > 2048 || medidas.outHeight / muestra > 2048) {
                muestra *= 2
            }

            val opciones = BitmapFactory.Options()
            opciones.inSampleSize = muestra
            entrada = context.contentResolver.openInputStream(uri) ?: return null
            val original = BitmapFactory.decodeStream(entrada, null, opciones)
            entrada.close()
            if (original == null) return null

            var foto = original
            val ladoMayor = maxOf(original.width, original.height)
            if (ladoMayor > 1024) {
                val factor = 1024f / ladoMayor
                foto = Bitmap.createScaledBitmap(
                    original,
                    (original.width * factor).toInt(),
                    (original.height * factor).toInt(),
                    true
                )
            }

            val carpeta = File(context.filesDir, "fotos")
            if (!carpeta.exists()) carpeta.mkdirs()
            val archivo = File(carpeta, "foto_" + System.currentTimeMillis() + ".jpg")
            val salida = FileOutputStream(archivo)
            foto.compress(Bitmap.CompressFormat.JPEG, 75, salida)
            salida.close()
            return archivo.absolutePath
        } catch (e: Exception) {
            return null
        }
    }
}
