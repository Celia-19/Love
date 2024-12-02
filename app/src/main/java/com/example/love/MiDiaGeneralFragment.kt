package com.example.love

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import okhttp3.*
import com.android.volley.Request.Method
import com.android.volley.Request
import org.json.JSONException
import org.json.JSONObject


class MiDiaGeneralFragment : Fragment() {

    private lateinit var tvPaqueteNombre: TextView
    private lateinit var tvFechaBoda: TextView
    private lateinit var tvSalon: TextView
    private lateinit var tvEstadoBoda: TextView
    private lateinit var tvEmpleadoNombre: TextView
    private lateinit var tvEmpleadoDepartamento: TextView
    private var usuarioId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recuperar usuario_id desde arguments
        usuarioId = arguments?.getString("usuario_id")

        if (usuarioId.isNullOrEmpty()) {
            Toast.makeText(context, "Error: Usuario no identificado. Por favor, intenta nuevamente.", Toast.LENGTH_LONG).show()
            activity?.finish()
        } else {
            Log.d("MiDiaGeneralFragment", "Usuario ID recuperado: $usuarioId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mi_dia_general, container, false)

        // Inicializar vistas
        tvPaqueteNombre = view.findViewById(R.id.tvPaqueteNombre)
        tvFechaBoda = view.findViewById(R.id.tvFechaBoda)
        tvSalon = view.findViewById(R.id.tvSalon)
        tvEstadoBoda = view.findViewById(R.id.tvEstadoBoda)
        tvEmpleadoNombre = view.findViewById(R.id.tvEmpleadoNombre)
        tvEmpleadoDepartamento = view.findViewById(R.id.tvEmpleadoDepartamento)

        cargarDatos()
        return view
    }

    private fun cargarDatos() {
        if (usuarioId.isNullOrEmpty()) {
            Toast.makeText(context, "Usuario no identificado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show()
            return
        }

        val url = "https://ladetechnologies.com/loveplan/getPaqueteYEmpleado.php?idUsuario=$usuarioId"
        Log.d("MiDiaGeneralFragment", "URL generada: $url")

        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(Request.Method.GET, url, { response ->
            Log.d("MiDiaGeneralFragment", "Respuesta del servidor: $response")
            try {
                val jsonObject = JSONObject(response)

                if (jsonObject.has("error")) {
                    val error = jsonObject.getString("error")
                    Toast.makeText(context, "Error del servidor: $error", Toast.LENGTH_LONG).show()
                    return@StringRequest
                }

                // Procesar datos
                tvPaqueteNombre.text = "Paquete: ${jsonObject.optString("paquete", "No especificado")}"
                tvFechaBoda.text = "Fecha: ${jsonObject.optString("fecha_boda", "No especificada")}"
                tvSalon.text = "Salón: ${jsonObject.optString("salon", "No especificado")}"

                val estado = jsonObject.optString("estado_boda", "Desconocido").lowercase()
                tvEstadoBoda.text = "Estado: ${estado.replaceFirstChar { it.uppercase() }}"
                tvEstadoBoda.setTextColor(
                    when (estado) {
                        "pendiente" -> Color.RED
                        "en proceso" -> Color.YELLOW
                        "finalizada" -> Color.GREEN
                        else -> Color.GRAY
                    }
                )

                tvEmpleadoNombre.text = "Encargado: ${jsonObject.optString("empleado_nombre", "Sin asignar")}"
                tvEmpleadoDepartamento.text = "Departamento: ${jsonObject.optString("empleado_departamento", "Sin asignar")}"

            } catch (e: JSONException) {
                Log.e("MiDiaGeneralFragment", "Error al procesar JSON: ${e.message}")
                Toast.makeText(context, "Error al procesar los datos del servidor.", Toast.LENGTH_LONG).show()
            }
        }, { error ->
            Log.e("MiDiaGeneralFragment", "Error en la solicitud: ${error.message}")
            Toast.makeText(context, "Error al conectar con el servidor.", Toast.LENGTH_LONG).show()
        })

        queue.add(request)
    }
}