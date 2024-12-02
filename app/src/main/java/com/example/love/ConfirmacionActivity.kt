package com.example.love

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.love.databinding.ActivityConfirmacionBinding

class ConfirmacionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacion)

        // Referencias a los elementos de la vista
        val tvThankYou: TextView = findViewById(R.id.tvThankYou)
        val btnGoToMyDay: Button = findViewById(R.id.btnGoToMyDay)

        // Obtener el número de tarjeta del Intent
        val cardNumber = intent.getStringExtra("cardNumber")

        if (cardNumber != null && cardNumber.length >= 4) {
            // Mostrar los últimos 4 dígitos de la tarjeta
            val lastFourDigits = cardNumber.takeLast(4)
            val paymentDetailsText =
                "El pago con la tarjeta terminación $lastFourDigits ha sido realizado con éxito."
            tvThankYou.text = paymentDetailsText
        } else {
            tvThankYou.text = "El pago ha sido realizado con éxito."
        }

        // Configurar el botón para navegar a Registro_bodaActivity
        btnGoToMyDay.setOnClickListener {
            val intent = Intent(this, Registro_bodaActivity::class.java)
            startActivity(intent)
            finish() // Finalizar esta actividad para que no quede en el stack
        }
    }
}