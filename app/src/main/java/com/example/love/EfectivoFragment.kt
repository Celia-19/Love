package com.example.love

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


class EfectivoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el diseño del fragmento
        val view = inflater.inflate(R.layout.fragment_efectivo, container, false)

        // Configurar el botón
        val btnPayCash: Button = view.findViewById(R.id.btnPayCash)
        btnPayCash.setOnClickListener {
            // Iniciar ContactoActivity
            val intent = Intent(activity, ContactosActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}