package com.example.love

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import org.json.JSONException
import org.json.JSONObject
import com.android.volley.Request


class ContactosActivity : AppCompatActivity()  {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactos)

        // Inicializa las vistas
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)

        // Configurar Toolbar
        setSupportActionBar(toolbar)  // Asegúrate de que este método esté aquí
        supportActionBar?.title = "Contactos"  // Título del Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  // Mostrar el icono de "home"

        // Inicializa ActionBarDrawerToggle
        toggle = ActionBarDrawerToggle(
            this,               // Contexto de la actividad
            drawerLayout,       // El DrawerLayout que contiene el menú lateral
            toolbar,            // El Toolbar donde se pondrá el ícono
            R.string.open_drawer,  // Texto accesible para abrir el menú
            R.string.close_drawer  // Texto accesible para cerrar el menú
        )

        // Sincroniza el estado del ícono con el DrawerLayout
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Configurar clics en NavigationView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> startActivity(Intent(this, HomeActivity::class.java))
                R.id.nav_anticipos -> startActivity(Intent(this, PaquetesActivity::class.java))
                R.id.nav_perfil -> startActivity(Intent(this, PerfilActivity::class.java))
                R.id.nav_mi_dia -> startActivity(Intent(this, MiDiaActivity::class.java))
                R.id.nav_logout -> logout()
            }
            drawerLayout.closeDrawers() // Cierra el menú después de seleccionar
            true
        }


        findViewById<Button>(R.id.btnAbrirWhatsApp).setOnClickListener {
            abrirWhatsApp("3334467037") // Cambia este número por el que desees
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
        return if (toggle.onOptionsItemSelected(item)) {
            true
        } else {
            super.onOptionsItemSelected(item)
        }
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
        if (usuarioId.isNullOrEmpty()) {
            // Maneja el caso si el usuario no está autenticado
            return
        }

        val url = "https://ladetechnologies.com/loveplan/obtener_perfil.php?usuario_id=$usuarioId"
        val requestQueue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
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
                    Toast.makeText(this, "Error al procesar los datos del servidor.", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
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

    // Función para abrir WhatsApp
    private fun abrirWhatsApp(phoneNumber: String) {
        val url = "https://api.whatsapp.com/send?phone=$phoneNumber"
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al abrir WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }
}