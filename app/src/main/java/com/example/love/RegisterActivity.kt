package com.example.love

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import com.android.volley.Request
import com.android.volley.RequestQueue
import java.io.IOException


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etFirstName = findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = findViewById<TextInputEditText>(R.id.etLastName)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val phone = etPhone.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Realizar la solicitud al servidor PHP
            registerUser(email, password, firstName, lastName, phone)
        }
    }

    private fun registerUser(email: String, password: String, firstName: String, lastName: String, phone: String) {
        val url = "https://ladetechnologies.com/loveplan/registro.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.optString("status", "error")
                    val message = jsonResponse.optString("message", "Respuesta inesperada del servidor")

                    if (status == "success") {
                        Toast.makeText(this, "Registro exitoso. Ahora inicia sesión.", Toast.LENGTH_SHORT).show()

                        // Navegar al LoginActivity
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Error de red: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["correo"] = email
                params["contraseña"] = password
                params["nombre"] = firstName
                params["apellido"] = lastName
                params["telefono"] = phone
                params["codigo"] = "clien-1234"
                return params
            }
        }

        queue.add(request)
    }
}