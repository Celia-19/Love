package com.example.love

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class PagosActivity : AppCompatActivity() {

    private lateinit var tvTotalAmount: TextView
    private lateinit var tvCurrentPayment: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagos)

        // Configurar Toolbar como ActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Habilitar botón de regresar

        // Referencias a vistas
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvCurrentPayment = findViewById(R.id.tvCurrentPayment)
        val radioGroup = findViewById<RadioGroup>(R.id.rgPaymentMethods)

        // Obtener datos del Intent
        val idPaquete = intent.getIntExtra("paquete_id", -1) // Valor por defecto -1 para indicar error
        if (idPaquete == -1) {
            Log.e("PaqueteDetallesActivity", "ID del paquete no recibido")
            Toast.makeText(this, "Error: ID del paquete no encontrado", Toast.LENGTH_SHORT).show()
            finish() // Opcional: Cierra la actividad si es un error crítico
        }
        val paqueteNombre = intent.getStringExtra("paquete_nombre") ?: "Paquete desconocido"
        val paquetePrecio = intent.getDoubleExtra("paquete_precio", 0.0)

        // Actualizar vistas con los datos del paquete
        actualizarVistas(paqueteNombre, paquetePrecio)

        // Configurar comportamiento del RadioGroup
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            handlePaymentMethodChange(checkedId)
        }

        // Cargar por defecto el fragmento inicial
        if (savedInstanceState == null) {
            loadFragment(TarjetaFragment())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Volver a la actividad anterior
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun actualizarVistas(nombre: String, precio: Double) {
        tvTotalAmount.text = "Total a pagar: $${"%.2f".format(precio)}"
        val primerPago = precio * 0.15
        tvCurrentPayment.text = "Monto a pagar (15%): $${"%.2f".format(primerPago)}"
    }

    private fun handlePaymentMethodChange(methodId: Int) {
        when (methodId) {
            R.id.rbCreditCard -> loadFragment(TarjetaFragment())
            R.id.rbCash -> loadFragment(EfectivoFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val paqueteNombre = intent.getStringExtra("paquete_nombre") ?: "Paquete desconocido"
        val paquetePrecio = intent.getDoubleExtra("paquete_precio", 0.0)
        val idCliente = intent.getIntExtra("idCliente", -1)
        val idPaquete = intent.getIntExtra("paquete_id", -1)

        if (idCliente == -1) {
            Toast.makeText(this, "ID de cliente no encontrado en Intent, verifica tu inicio de sesión.", Toast.LENGTH_SHORT).show()
            return
        }

        val bundle = Bundle().apply {
            putString("paquete_nombre", paqueteNombre)
            putDouble("paquete_precio", paquetePrecio)
            putInt("idCliente", idCliente)
            putInt("paquete_id", idPaquete)
            putDouble("monto_anticipo", paquetePrecio * 0.15) // Calcula el anticipo
        }
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val selectedId = findViewById<RadioGroup>(R.id.rgPaymentMethods).checkedRadioButtonId
        outState.putInt("SELECTED_RADIO_ID", selectedId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val selectedId = savedInstanceState.getInt("SELECTED_RADIO_ID", R.id.rbCreditCard)
        findViewById<RadioGroup>(R.id.rgPaymentMethods).check(selectedId)
    }

    private fun getEncryptedSharedPreferences(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "preferencias_login",
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}