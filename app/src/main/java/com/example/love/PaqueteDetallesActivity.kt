package com.example.love

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import com.example.love.ImagenAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.viewpager2.widget.ViewPager2
import android.util.Base64
import com.bumptech.glide.Glide
import com.example.love.databinding.ActivityPaqueteDetallesBinding

class PaqueteDetallesActivity : AppCompatActivity() {

    private lateinit var ivDetalleImagen: ImageView
    private lateinit var tvDetalleNombre: TextView
    private lateinit var tvDetalleBeneficios: TextView
    private lateinit var tvDetallePrecio: TextView
    private lateinit var viewPagerImagenes: ViewPager2
    private lateinit var btnComprarPaquete: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paquete_detalles)

        // Vinculación de vistas
        ivDetalleImagen = findViewById(R.id.ivDetalleImagen)
        tvDetalleNombre = findViewById(R.id.tvDetalleNombre)
        tvDetalleBeneficios = findViewById(R.id.tvDetalleBeneficios)
        tvDetallePrecio = findViewById(R.id.tvDetallePrecio)
        viewPagerImagenes = findViewById(R.id.viewPagerImagenes)
        btnComprarPaquete = findViewById(R.id.btnComprarPaquete)

        // Configurar Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Detalles del Paquete"
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Obtener datos desde el Intent
        val idPaquete = intent.getIntExtra("paquete_id", -1) // Valor por defecto -1 para indicar error
        if (idPaquete == -1) {
            Log.e("PaqueteDetallesActivity", "ID del paquete no recibido")
            Toast.makeText(this, "Error: ID del paquete no encontrado", Toast.LENGTH_SHORT).show()
            finish() // Opcional: Cierra la actividad si es un error crítico
        }
        val nombre = intent.getStringExtra("paquete_nombre") ?: "Nombre no disponible"
        val beneficios = intent.getStringExtra("paquete_descripcion") ?: "Beneficios no disponibles"
        val precio = intent.getDoubleExtra("paquete_precio", 0.0)
        val imagenesBase64 = intent.getStringArrayListExtra("paquete_imagenes") ?: arrayListOf()

        Log.d("PaqueteDetallesActivity", "Imágenes recibidas: $imagenesBase64")

        // Asignar datos a las vistas
        tvDetalleNombre.text = nombre
        tvDetalleBeneficios.text = beneficios
        tvDetallePrecio.text = "$${precio}" // Utiliza un recurso de string para mejor formato

        // Mostrar imagen destacada
        mostrarImagenDestacada(imagenesBase64)

        // Configurar ViewPager para mostrar imágenes adicionales
        configurarViewPager(imagenesBase64)

        // Configurar botón "Comprar Paquete"
        configurarBotonCompra(nombre, precio, idPaquete)
    }

    private fun mostrarImagenDestacada(imagenesBase64: ArrayList<String>) {
        if (imagenesBase64.isNotEmpty()) {
            val bitmap = decodeBase64ToBitmap(imagenesBase64[0])
            if (bitmap != null) {
                ivDetalleImagen.setImageBitmap(bitmap)
            } else {
                ivDetalleImagen.setImageResource(R.drawable.ic_error) // Imagen de error
            }
        } else {
            ivDetalleImagen.setImageResource(R.drawable.paquete_basico) // Imagen predeterminada
        }
    }

    private fun configurarViewPager(imagenesBase64: ArrayList<String>) {
        if (imagenesBase64.isNotEmpty()) {
            val adapter = ImagenAdapter(this, imagenesBase64)
            viewPagerImagenes.adapter = adapter
            viewPagerImagenes.visibility = View.VISIBLE
        } else {
            viewPagerImagenes.visibility = View.GONE
            Toast.makeText(this, "No hay imágenes disponibles", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configurarBotonCompra(nombre: String, precio: Double, idPaquete: Int) {
        btnComprarPaquete.setOnClickListener {
            if (nombre.isNotEmpty() && precio > 0) {
                val sharedPreferences = getEncryptedSharedPreferences()
                val usuarioId = sharedPreferences.getString("usuario_id", null)

                if (usuarioId.isNullOrEmpty()) {
                    Toast.makeText(this, "Usuario ID no encontrado en SharedPreferences", Toast.LENGTH_SHORT).show()
                    Log.e("CompraPaquete", "Usuario ID no encontrado")
                    return@setOnClickListener
                }

                // Crear intent y pasar datos al siguiente Activity
                val intent = Intent(this, PagosActivity::class.java).apply {
                    putExtra("paquete_nombre", nombre)
                    putExtra("paquete_precio", precio)
                    putExtra("idCliente", usuarioId.toInt()) // Enviar usuarioId como idCliente
                    putExtra("paquete_id", idPaquete) // Enviar también el idPaquete
                    Log.d("PaqueteDetallesActivity", "ID del paquete recibido: $idPaquete")
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Datos inválidos del paquete", Toast.LENGTH_SHORT).show()
            }
        }
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

    fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val base64Data = if (base64Str.startsWith("data:image")) {
                base64Str.substring(base64Str.indexOf(",") + 1)
            } else {
                base64Str
            }
            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }
}
