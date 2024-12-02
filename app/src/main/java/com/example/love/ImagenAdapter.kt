package com.example.love

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import android.util.Base64
import java.io.File

class ImagenAdapter(
    private val context: Context,
    private val imagenes: List<String>
) : RecyclerView.Adapter<ImagenAdapter.ImagenViewHolder>() {

    class ImagenViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagenViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_imagen, parent, false)
        return ImagenViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImagenViewHolder, position: Int) {
        val base64Str = imagenes[position]

        // Decodificar Base64 a Bitmap
        val bitmap = decodeBase64ToBitmap(base64Str)

        if (bitmap != null) {
            holder.imageView.setImageBitmap(bitmap)
        } else {
            holder.imageView.setImageResource(R.drawable.ic_error)
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

    override fun getItemCount(): Int = imagenes.size
}