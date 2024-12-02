package com.example.love

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import okhttp3.*
import com.android.volley.Request.Method
import android.util.Base64
import com.android.volley.Request
import java.io.ByteArrayOutputStream


class PaquetesActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paquetes)

        setupUI()
        setupMenu()
        cargarPaquetes()
    }

    private fun setupUI() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerPaquetes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "Paquetes Disponibles"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupMenu() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_anticipos -> startActivity(Intent(this, PaquetesActivity::class.java))
                R.id.nav_perfil -> startActivity(Intent(this, PerfilActivity::class.java))
                R.id.nav_mi_dia -> startActivity(Intent(this, MiDiaActivity::class.java))
                R.id.nav_logout -> logout()
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun cargarPaquetes() {
        val url = "https://ladetechnologies.com/loveplan/obtener_paquetes.php"
        val request = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                val paquetes = mutableListOf<Paquete>()

                // Agregar paquete fijo
                paquetes.add(
                    Paquete(
                        nombre = "Paquete Personalizado",
                        precio = 0.0,
                        imagenes = listOf(),
                        beneficios = listOf("Personaliza todos los aspectos de tu evento"),
                        idPaquete = 1
                    )
                )

                // Procesar los paquetes desde el servidor
                for (i in 0 until response.length()) {
                    val json = response.getJSONObject(i)
                    val imagenesList = mutableListOf<String>()

                    val imagenesArray = json.optJSONArray("imagenes")
                    if (imagenesArray != null) {
                        for (j in 0 until imagenesArray.length()) {
                            imagenesList.add(imagenesArray.getString(j)) // Base64 String
                        }
                    }

                    paquetes.add(
                        Paquete(
                            idPaquete = json.getInt("idPaquete"), // Obtener ID desde el servidor
                            nombre = json.getString("nombre"),
                            precio = json.getDouble("precio"),
                            imagenes = imagenesList,
                            beneficios = listOf(json.optString("descripcion", "Sin descripción"))
                        )
                    )
                }

                // Configurar RecyclerView con los paquetes cargados
                recyclerView.adapter = PaqueteAdapter(this, paquetes) { paquete ->
                    if (paquete.nombre == "Paquete Personalizado") {
                        // Redirigir a PersonalizaActivity
                        val intent = Intent(this, PersonalizaActivity::class.java)
                        startActivity(intent)
                    } else {

                        // Redirigir a PaqueteDetallesActivity

                        val intent = Intent(this, PaqueteDetallesActivity::class.java).apply {
                            putExtra("paquete_nombre", paquete.nombre) // Enviar el nombre del paquete
                            putExtra("paquete_precio", paquete.precio)
                            putExtra("paquete_descripcion", paquete.beneficios.joinToString("\n"))
                            putExtra("paquete_id", paquete.idPaquete) // Asegúrate de que el modelo 'Paquete' tenga un campo 'id'

                            // Convertir imágenes Base64 a Bitmap y enviarlas
                            val imagenesBitmap = paquete.imagenes.mapNotNull { decodeBase64ToBitmap(it) }
                            if (imagenesBitmap.isNotEmpty()) {
                                val baos = ByteArrayOutputStream()
                                imagenesBitmap[0].compress(Bitmap.CompressFormat.PNG, 100, baos)
                                putExtra("paquete_imagen", baos.toByteArray())
                            }
                        }
                        startActivity(intent)
                    }
                }
            },
            { error ->
                Toast.makeText(this, "Error al cargar los paquetes: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}