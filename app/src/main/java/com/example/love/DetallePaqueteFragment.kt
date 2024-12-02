package com.example.love

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class DetallePaqueteFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductosAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_detalle_paquete, container, false)

        // Inicializar RecyclerView
        recyclerView = view.findViewById(R.id.recyclerProductos)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Simulación de datos
        val productos = listOf(
            Producto("Mesa de dulces", "pendiente"),
            Producto("Decoración floral", "en proceso"),
            Producto("Catering", "completado"),
            Producto("Música en vivo", "pendiente")
        )

        // Configurar adaptador
        adapter = ProductosAdapter(productos)
        recyclerView.adapter = adapter

        return view
    }
}