package com.example.love

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import okhttp3.*
import com.android.volley.Response
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import fi.iki.elonen.NanoHTTPD
import java.net.URLDecoder



class TarjetaFragment : Fragment() {

    private lateinit var etCardHolderName: EditText
    private lateinit var etCardNumber: EditText
    private lateinit var etExpirationDate: EditText
    private lateinit var etCVV: EditText
    private lateinit var btnPayCard: Button
    private lateinit var server: MobileServer
    private var idPaquete: Int = -1

    private var idCliente: Int = -1 // Inicializar el ID del cliente como no encontrado

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tarjeta, container, false)

        // Asignar las vistas
        btnPayCard = view.findViewById(R.id.btnPayCard)
        etCardHolderName = view.findViewById(R.id.etCardHolderName)
        etCardNumber = view.findViewById(R.id.etCardNumber)
        etExpirationDate = view.findViewById(R.id.etExpirationDate)
        etCVV = view.findViewById(R.id.etCVV)

        // Obtener el ID del cliente desde los argumentos
        // Obtén los datos del paquete y el cliente

        idCliente = arguments?.getInt("idCliente", -1) ?: -1
        idPaquete = arguments?.getInt("paquete_id", -1) ?: -1

        // Verificar si el idPaquete es válido
        if (idPaquete == -1) {
            Toast.makeText(requireContext(), "ID de paquete inválido", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("TarjetaFragment", "ID del Paquete recibido: $idPaquete")
        }
        val paqueteNombre = arguments?.getString("paquete_nombre", "Paquete desconocido") ?: ""
        val paquetePrecio = arguments?.getDouble("paquete_precio") ?: 0.0
        val montoAnticipo = arguments?.getDouble("monto_anticipo", 0.0)


        if (idCliente == -1 || paqueteNombre.isEmpty() || paquetePrecio <= 0) {
            Toast.makeText(requireContext(), "Datos del paquete o cliente inválidos", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("TarjetaFragment", "Datos recibidos: idCliente=$idCliente, paquete=$paqueteNombre, precio=$paquetePrecio")
        }

        // Iniciar el servidor
        iniciarServidor()
        obtenerIdCliente()

        // Configurar el botón de pago
        btnPayCard.setOnClickListener {
            val paqueteNombre = arguments?.getString("paquete_nombre") ?: ""
            val paquetePrecio = arguments?.getDouble("paquete_precio") ?: 0.0
            val montoAnticipo = paquetePrecio * 0.15
            val cardNumber = etCardNumber.text.toString()

            // Validación de los datos
            if (idCliente == -1 || paquetePrecio <= 0) {
                Toast.makeText(requireContext(), "ID de cliente o datos del paquete inválidos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validación de los campos del formulario
            val cardHolderName = etCardHolderName.text.toString()

            if (cardHolderName.isEmpty() || cardNumber.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Enviar los datos al servidor
            enviarDatosAlServidor(idCliente, paqueteNombre, montoAnticipo)

            // Generar la notificación
            generarNotificacion("Pago de $paqueteNombre por $montoAnticipo realizado con éxito.")

            // Validación de los campos del formulario
            if (cardNumber.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mostrar el fragment de confirmación con los datos
            val confirmationDialog = PaymentConfirmationDialogFragment.newInstance(paqueteNombre, paquetePrecio, cardNumber)
            confirmationDialog.show(parentFragmentManager, "PaymentConfirmationDialog")
        }


        return view
    }

    private fun obtenerIdCliente() {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val sharedPreferences = EncryptedSharedPreferences.create(
                "preferencias_login", // Cambiar a "preferencias_login"
                masterKeyAlias,
                requireContext(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            val usuarioId = sharedPreferences.getString("usuario_id", null)

            if (usuarioId.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "ID de cliente no encontrado en SharedPreferences", Toast.LENGTH_SHORT).show()
                idCliente = -1
            } else {
                idCliente = usuarioId.toIntOrNull() ?: -1
            }
        } catch (e: Exception) {
            Log.e("TarjetaFragment", "Error al obtener el ID del cliente: ${e.message}")
            idCliente = -1
        }
    }

    private fun enviarDatosAlServidor(idCliente: Int, paqueteNombre: String, montoAnticipo: Double) {
        val url = "https://ladetechnologies.com/loveplan/guardar_pago.php"
        val requestQueue = Volley.newRequestQueue(requireContext())

        val params = HashMap<String, String>()
        params["idCliente"] = idCliente.toString()
        params["paquete_id"] = idPaquete.toString()
        params["paquete_nombre"] = paqueteNombre
        params["paquete_precio"] = montoAnticipo.toString() // Enviar el monto del anticipo

        val request = object : StringRequest(Method.POST, url,
            Response.Listener { response ->
                Toast.makeText(requireContext(), "Pago registrado con éxito", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                Toast.makeText(requireContext(), "Error al procesar el pago", Toast.LENGTH_SHORT).show()
            }) {
            override fun getParams(): Map<String, String> = params
        }

        requestQueue.add(request)
    }

    private fun generarNotificacion(mensaje: String) {
        val channelId = "notificaciones_pago"
        val channelName = "Notificaciones de Pago"

        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear un canal de notificación para Android 8.0 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Crear la notificación
        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Ícono de la notificación
            .setContentTitle("Notificación de Pago") // Título
            .setContentText(mensaje) // Mensaje
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridad
            .setAutoCancel(true) // Cancelar automáticamente al hacer clic
            .build()

        // Mostrar la notificación
        notificationManager.notify(1, notification)
    }

    private fun iniciarServidor() {
        server = MobileServer(requireContext(), 8080) // Pasar el contexto al servidor
        try {
            server.start() // Inicia el servidor embebido
            Log.d("TarjetaFragment", "Servidor móvil iniciado en el puerto 8080")
            Toast.makeText(requireContext(), "Servidor iniciado en el puerto 8080", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("TarjetaFragment", "Error al iniciar el servidor: ${e.message}")
            Toast.makeText(requireContext(), "Error al iniciar el servidor", Toast.LENGTH_SHORT).show()
        }
    }
}
