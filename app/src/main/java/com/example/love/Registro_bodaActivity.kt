package com.example.love

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import android.app.ProgressDialog
import com.android.volley.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Registro_bodaActivity : AppCompatActivity() {

    private lateinit var etGroomName: EditText
    private lateinit var etBrideName: EditText
    private lateinit var etWeddingDate: EditText
    private lateinit var etGuests: EditText
    private lateinit var btnSubmitWedding: Button
    private var idCliente: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_boda)

        etGroomName = findViewById(R.id.etGroomName)
        etBrideName = findViewById(R.id.etBrideName)
        etWeddingDate = findViewById(R.id.etWeddingDate)
        etGuests = findViewById(R.id.etGuests)
        btnSubmitWedding = findViewById(R.id.btnSubmitWedding)

        val sharedPreferences = getEncryptedSharedPreferences()
        idCliente = sharedPreferences.getString("usuario_id", null)

        if (idCliente.isNullOrEmpty()) {
            Toast.makeText(this, "No se pudo obtener el ID del cliente", Toast.LENGTH_SHORT).show()
            finish()
        }

        etWeddingDate.setOnClickListener { showDatePickerDialog() }

        btnSubmitWedding.setOnClickListener {
            val groomName = etGroomName.text.toString()
            val brideName = etBrideName.text.toString()
            val weddingDate = etWeddingDate.text.toString()
            val guests = etGuests.text.toString()

            if (groomName.isNotEmpty() && brideName.isNotEmpty() && weddingDate.isNotEmpty() && guests.isNotEmpty()) {
                if (idCliente != null) {
                    val weddingDateFormatted = validateAndFormatDate(weddingDate)
                    if (weddingDateFormatted != null) {
                        registerWedding(idCliente!!, groomName, brideName, weddingDateFormatted, guests.toInt())
                    } else {
                        Toast.makeText(this, "Formato de fecha inválido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "No se pudo obtener el ID del cliente", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                etWeddingDate.setText(date)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun validateAndFormatDate(date: String): String? {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.format(dateFormat.parse(date)!!)
        } catch (e: ParseException) {
            null
        }
    }

    private fun registerWedding(idCliente: String, groomName: String, brideName: String, weddingDate: String, guests: Int) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Registrando boda...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val url = "https://ladetechnologies.com/loveplan/registro_boda.php"
        val queue = Volley.newRequestQueue(this)

        val request = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                progressDialog.dismiss()
                try {
                    Log.d("RegistroBoda", "Respuesta del servidor: $response")
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")
                    val message = jsonResponse.getString("message")

                    if (status == "success") {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, PerfilActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Log.e("RegistroBoda", "Error al procesar la respuesta del servidor: ${e.message}")
                    Toast.makeText(this, "Error al procesar la respuesta del servidor", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                progressDialog.dismiss()
                Log.e("RegistroBoda", "Error de red: ${error.message}")
                Toast.makeText(this, "Error de red: ${error.message ?: "desconocido"}", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "idCliente" to idCliente,
                    "nombreNovio" to groomName,
                    "nombreNovia" to brideName,
                    "numInvitados" to guests.toString(),
                    "fechaBoda" to weddingDate
                )
            }
        }

        queue.add(request)
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