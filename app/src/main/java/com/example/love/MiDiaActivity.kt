package com.example.love

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import org.json.JSONException
import org.json.JSONObject

class MiDiaActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mi_dia)

        // Inicializa las vistas
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        toolbar = findViewById(R.id.toolbar)

        // Configurar Toolbar
        setSupportActionBar(toolbar)  // Asegúrate de que este método esté aquí
        supportActionBar?.title = "Mi día"  // Título del Toolbar
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
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish() // Evita que regrese a esta Activity
                }
                R.id.nav_anticipos -> {
                    val intent = Intent(this, PaquetesActivity::class.java)
                    startActivity(intent)
                    finish() // Evita que regrese a esta Activity
                }
                R.id.nav_perfil -> {
                val intent = Intent(this, PerfilActivity::class.java)
                startActivity(intent)
                finish() // Evita que regrese a esta Activity
            }
                R.id.nav_mi_dia-> {
                    val intent = Intent(this, MiDiaActivity::class.java)
                    startActivity(intent)
                    finish() // Evita que regrese a esta Activity
                }
                R.id.nav_logout -> {
                    logout()
                }
            }
            drawerLayout.closeDrawers() // Cierra el menú después de seleccionar
            true
        }

        // Obtener SharedPreferences
        val sharedPreferences = getEncryptedSharedPreferences()

        // Validar usuario_id
        val usuarioId = sharedPreferences.getString("usuario_id", null)
        if (usuarioId.isNullOrEmpty()) {
            Log.e("MiDiaActivity", "Error: Usuario ID no encontrado en SharedPreferences")
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("MiDiaActivity", "Usuario ID recuperado: $usuarioId")

        // Configurar Toolbar, Drawer y NavigationView
        setupToolbarAndDrawer()

        // Cargar el fragmento inicial con el usuarioId
        if (savedInstanceState == null) {
            val fragment = MiDiaGeneralFragment()
            val bundle = Bundle()
            bundle.putString("usuario_id", usuarioId) // Pasar el usuario_id
            fragment.arguments = bundle
            replaceFragment(fragment)
        }
        obtenerNombreUsuario()
    } //cierre del ov


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
        val sharedPreferences = getEncryptedSharedPreferences()
        sharedPreferences.edit().clear().apply()

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
        val stringRequest = StringRequest(
            Request.Method.GET, url,
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



    private fun setupToolbarAndDrawer() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    val fragment = MiDiaGeneralFragment()
                    val bundle = Bundle()
                    bundle.putString("usuario_id", getUsuarioId())
                    fragment.arguments = bundle
                    replaceFragment(fragment)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun getEncryptedSharedPreferences(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            "preferencias_login",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun getUsuarioId(): String {
        val sharedPreferences = getEncryptedSharedPreferences()
        return sharedPreferences.getString("usuario_id", "") ?: ""
    }
}