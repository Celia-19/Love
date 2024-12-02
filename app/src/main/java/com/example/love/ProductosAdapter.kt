package com.example.love

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductosAdapter(private val productos: List<Producto>) :
    RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val lineaEstado: View = view.findViewById(R.id.lineaEstado)
        val nombreProducto: TextView = view.findViewById(R.id.tvNombreProducto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.nombreProducto.text = producto.nombre

        // Cambiar color del indicador según el estado
        val color = when (producto.estado) {
            "pendiente" -> R.color.red
            "en proceso" -> R.color.yellow
            "completado" -> R.color.green
            else -> R.color.gray
        }
        holder.lineaEstado.setBackgroundResource(color)
    }



    override fun getItemCount(): Int = productos.size
}