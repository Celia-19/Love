package com.example.love

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import okhttp3.*
import com.google.android.material.textfield.TextInputEditText
import java.io.IOException
import com.android.volley.Request
import java.io.File
import java.io.FileOutputStream
import com.android.volley.Response

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import org.json.JSONException
import org.json.JSONObject

class PerfilActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil)

        // Inicializa las vistas
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)

        // Configurar Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Perfil de Usuario"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        // Configurar clics en NavigationView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this, Registro_bodaActivity::class.java))
                R.id.nav_anticipos -> startActivity(Intent(this, PaquetesActivity::class.java))
                R.id.nav_perfil -> startActivity(Intent(this, PerfilActivity::class.java))
                R.id.nav_mi_dia -> startActivity(Intent(this, MiDiaActivity::class.java))
                R.id.nav_logout -> logout()
            }
            drawerLayout.closeDrawers() // Cierra el menú después de seleccionar
            true
        }

        // Carga inicial del fragmento si no está cargado
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PerfilFragment()) // Fragmento por defecto
                .commit()
        }
        obtenerNombreUsuario()
    }

    // Inflar el menú horizontal en la Toolbar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_horizontal, menu)
        return true
    }

    // Manejar clics en el menú de la Toolbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START) // Abre el menú lateral
                true
            }
            R.id.action_settings -> {
                // Lógica para "Configuración"
                true
            }
            R.id.action_settings -> {
                // Lógica para "Ayuda"
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Abrir un fragmento en el contenedor principal
    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Lógica para cerrar sesión
    private fun logout() {
        // Lógica para cerrar sesión (limpiar SharedPreferences o autenticación)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun obtenerNombreUsuario() {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "preferencias_login",
            masterKeyAlias,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val usuarioId = sharedPreferences.getString("usuario_id", "")
        Log.d("PerfilActivity", "Usuario ID recuperado: $usuarioId")

        if (usuarioId.isNullOrEmpty()) {
            Log.e("PerfilActivity", "Usuario ID no encontrado en SharedPreferences")
            Toast.makeText(
                this,
                "Usuario no encontrado. Por favor, inicia sesión nuevamente.",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val url = "https://ladetechnologies.com/loveplan/obtener_perfil.php?usuario_id=$usuarioId"
        Log.d("PerfilActivity", "URL: $url")

        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                Log.d("PerfilActivity", "Respuesta del servidor: $response")
                try {
                    val jsonObject = JSONObject(response)
                    if (jsonObject.getString("status") == "success") {
                        val nombre = jsonObject.optString("Nombre", "Usuario no disponible")
                        actualizarNombreEnMenuLateral(nombre)
                    } else {
                        val mensaje = jsonObject.getString("message")
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Log.e("PerfilActivity", "Error al procesar los datos del usuario: ${e.message}")
                    Toast.makeText(this, "Error al procesar los datos del servidor.", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Log.e("PerfilActivity", "Error en la conexión con el servidor: ${error.message}")
                Toast.makeText(this, "Error al conectar con el servidor.", Toast.LENGTH_SHORT).show()
            })

        requestQueue.add(stringRequest)
    }

    private fun actualizarNombreEnMenuLateral(nombre: String) {
        val headerView = navigationView.getHeaderView(0)
        val navUsername = headerView.findViewById<TextView>(R.id.nav_username)

        // Actualiza el nombre del usuario en el menú lateral
        navUsername.text = nombre
    }

}
