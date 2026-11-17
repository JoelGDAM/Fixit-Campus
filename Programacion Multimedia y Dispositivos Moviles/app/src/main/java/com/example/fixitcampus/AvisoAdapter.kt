package com.example.fixitcampus

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

// Adaptador de las tarjetas de avisos
class AvisoAdapter(
    private var lista: List<Aviso>,
    private val alTocar: (Aviso) -> Unit
) : RecyclerView.Adapter<AvisoAdapter.VistaAviso>() {

    class VistaAviso(v: View) : RecyclerView.ViewHolder(v) {
        val tvCodigo: TextView = v.findViewById(R.id.tvCodigo)
        val tvUbicacion: TextView = v.findViewById(R.id.tvUbicacion)
        val tvDescripcion: TextView = v.findViewById(R.id.tvDescripcion)
        val tvFecha: TextView = v.findViewById(R.id.tvFecha)
        val tvPrioridad: TextView = v.findViewById(R.id.tvPrioridad)
        val tvBadgeEstado: TextView = v.findViewById(R.id.tvBadgeEstado)
        val vPunto: View = v.findViewById(R.id.vPuntoPrioridad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VistaAviso {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_aviso, parent, false)
        return VistaAviso(vista)
    }

    override fun getItemCount(): Int = lista.size

    override fun onBindViewHolder(holder: VistaAviso, position: Int) {
        val aviso = lista[position]
        val contexto = holder.itemView.context

        holder.tvCodigo.text = aviso.codigo
        holder.tvUbicacion.text = aviso.espacio
        holder.tvDescripcion.text = aviso.descripcion
        holder.tvFecha.text = DbHelper.fechaRelativa(aviso.fecha)
        holder.tvPrioridad.text = aviso.prioridad

        // Punto y texto con el color de la prioridad
        val color = colorPrioridad(contexto, aviso.prioridad)
        holder.tvPrioridad.setTextColor(color)
        holder.vPunto.backgroundTintList = ColorStateList.valueOf(color)

        if (aviso.estado == "RESUELTA") {
            holder.tvBadgeEstado.text = "RESUELTA"
            holder.tvBadgeEstado.setBackgroundResource(R.drawable.bg_badge_resuelta)
            holder.tvBadgeEstado.setTextColor(ContextCompat.getColor(contexto, R.color.resuelta_texto))
        } else {
            holder.tvBadgeEstado.text = "ABIERTA"
            holder.tvBadgeEstado.setBackgroundResource(R.drawable.bg_badge_abierta)
            holder.tvBadgeEstado.setTextColor(ContextCompat.getColor(contexto, R.color.texto))
        }

        holder.itemView.setOnClickListener { alTocar(aviso) }
    }

    fun actualizar(nuevos: List<Aviso>) {
        lista = nuevos
        notifyDataSetChanged()
    }

    companion object {
        fun colorPrioridad(contexto: Context, prioridad: String): Int = when (prioridad) {
            "Urgente" -> ContextCompat.getColor(contexto, R.color.urgente)
            "Alta" -> ContextCompat.getColor(contexto, R.color.alta)
            "Media" -> ContextCompat.getColor(contexto, R.color.media)
            else -> ContextCompat.getColor(contexto, R.color.baja)
        }
    }
}
