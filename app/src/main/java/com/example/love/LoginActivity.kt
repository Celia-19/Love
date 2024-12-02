package com.example.love

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import java.util.concurrent.Executor
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request



class LoginActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<Button>(R.id.btnLogin)
        val skipLogin = findViewById<TextView>(R.id.tvSkipLogin)
        val register = findViewById<TextView>(R.id.tvRegister)

        // Cargar datos guardados en SharedPreferences
        cargarDatosGuardados()

        loginButton.setOnClickListener {
            val correo = findViewById<TextInputEditText>(R.id.etEmailPhone).text.toString()
            val contrasena = findViewById<TextInputEditText>(R.id.etPassword).text.toString()

            if (correo.isNotEmpty() && contrasena.isNotEmpty()) {
                authenticateUser(correo, contrasena)
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        skipLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("skipLogin", true)
            startActivity(intent)
        }
    }

    private fun authenticateUser(correo: String, contrasena: String) {
        val url = "https://ladetechnologies.com/loveplan/login.php"

        val formBody = FormBody.Builder()
            .add("correo", correo)
            .add("contrasena", contrasena)
            .build()

        val request = okhttp3.Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Error de conexión: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Error en la respuesta", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    val responseData = response.body?.string()
                    Log.d("LoginActivity", "Response: $responseData")

                    if (responseData != null) {
                        try {
                            val jsonObject = JSONObject(responseData)
                            val status = jsonObject.getString("status")

                            if (status == "success") {
                                val usuarioId = jsonObject.optString("usuario_id", null)

                                if (usuarioId != null) {
                                    guardarDatos(correo, contrasena, usuarioId)

                                    // Iniciar PerfilActivity
                                    runOnUiThread {
                                        val intent = Intent(this@LoginActivity, PerfilActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(this@LoginActivity, "Usuario ID no recibido", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                runOnUiThread {
                                    val mensaje = jsonObject.optString("message", "Usuario o contraseña incorrectos")
                                    Toast.makeText(this@LoginActivity, mensaje, Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this@LoginActivity, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show()
                                Log.e("LoginActivity", "Error al procesar JSON: ${e.message}")
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
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

    private fun guardarDatos(usuario: String, contrasena: String, usuarioId: String) {
        val sharedPreferences = getEncryptedSharedPreferences()
        with(sharedPreferences.edit()) {
            putString("usuario", usuario)
            putString("contrasena", contrasena)
            putString("usuario_id", usuarioId)
            putBoolean("isLoggedIn", true)
            apply()
        }
        Log.d("LoginActivity", "Datos guardados: Usuario ID = $usuarioId")
    }

    private fun cargarDatosGuardados() {
        try {
            val sharedPreferences = getEncryptedSharedPreferences()
            val usuario = sharedPreferences.getString("usuario", "")
            val contrasena = sharedPreferences.getString("contrasena", "")
            val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

            val usuarioEditText = findViewById<TextInputEditText>(R.id.etEmailPhone)
            val contrasenaEditText = findViewById<TextInputEditText>(R.id.etPassword)

            if (isLoggedIn && !usuario.isNullOrEmpty() && !contrasena.isNullOrEmpty()) {
                val intent = Intent(this, PerfilActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                usuarioEditText.setText(usuario)
                contrasenaEditText.setText(contrasena)
            }
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error al cargar datos: ${e.message}")
        }
    }
}