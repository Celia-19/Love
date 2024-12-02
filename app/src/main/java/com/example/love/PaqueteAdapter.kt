package com.example.love

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Base64
import com.bumptech.glide.Glide

class PaqueteAdapter(
    private val context: Context,
    private val paquetes: List<Paquete>,
    private val onClick: (Paquete) -> Unit
) : RecyclerView.Adapter<PaqueteAdapter.PaqueteViewHolder>() {

    class PaqueteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvPaqueteNombre)
        val imagen: ImageView = view.findViewById(R.id.ivPaqueteImagen)
        val precio: TextView = view.findViewById(R.id.tvPaquetePrecio)
        val btnVerDetalles: Button = view.findViewById(R.id.btnVerDetalles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaqueteViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_paquete, parent, false)
        return PaqueteViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaqueteViewHolder, position: Int) {
        val paquete = paquetes[position]
        holder.nombre.text = paquete.nombre ?: "Nombre no disponible"

        // Decodificar y cargar imagen Base64 en el ImageView
        val base64Image = paquete.imagenes.firstOrNull()
        if (base64Image != null) {
            val bitmap = decodeBase64ToBitmap(base64Image)
            if (bitmap != null) {
                holder.imagen.setImageBitmap(bitmap)
            } else {
                holder.imagen.setImageResource(R.drawable.ic_error) // Imagen de error
            }
        } else {
            holder.imagen.setImageResource(R.drawable.ic_personalizado) // Placeholder
        }

        holder.precio.text = paquete.precio.let {
            String.format("$%.2f", it)
        } ?: "Precio personalizado"

        // Configurar el click listener del botón
        holder.btnVerDetalles.setOnClickListener {
            onClick(paquete) // Llama la función onClick con el paquete seleccionado
        }
    }

    fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str.split(",")[1], Base64.DEFAULT) // Eliminar prefijo data:image
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }

    override fun getItemCount(): Int = paquetes.size
}