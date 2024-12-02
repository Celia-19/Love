package com.example.love

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import com.android.volley.Request
import java.io.File
import java.io.FileOutputStream
import com.android.volley.Response




class PerfilFragment : Fragment() {
    private lateinit var profileImage: ImageView
    private lateinit var btnEditProfilePic: ImageButton
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvNovio: TextView
    private lateinit var tvNovia: TextView
    private lateinit var tvFecha: TextView
    private lateinit var btnChangePassword: Button
    private lateinit var etRFC: EditText
    private lateinit var etBillingName: EditText
    private lateinit var etBillingAddress: EditText
    private lateinit var etBillingPostalCode: EditText
    private lateinit var etBillingFiscalRegime: EditText
    private lateinit var cbEnableBilling: CheckBox
    private lateinit var btnSubmitBilling: Button
    private lateinit var btnLogout: Button
    private lateinit var tvBillingInfo: TextView
    private val PICK_IMAGE_REQUEST = 1
    private val TAKE_PICTURE_REQUEST = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        // Inicializar vistas
        profileImage = view.findViewById(R.id.profileImage)
        btnEditProfilePic = view.findViewById(R.id.btnEditProfilePic)
        tvUsername = view.findViewById(R.id.tvUsername)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvNovio = view.findViewById(R.id.tvNovio)
        tvNovia = view.findViewById(R.id.tvNovia)
        tvFecha = view.findViewById(R.id.tvFecha)
        tvBillingInfo = view.findViewById(R.id.tvBillingInfo)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)
        cbEnableBilling = view.findViewById(R.id.cbEnableBilling)
        etRFC = view.findViewById(R.id.etRFC)
        etBillingName = view.findViewById(R.id.etBillingName)
        etBillingAddress = view.findViewById(R.id.etBillingAddress)
        etBillingPostalCode = view.findViewById(R.id.etBillingPostalCode)
        etBillingFiscalRegime = view.findViewById(R.id.etBillingFiscalRegime)
        btnSubmitBilling = view.findViewById(R.id.btnSubmitBilling)
        btnLogout = view.findViewById(R.id.btnLogout)

        obtenerDatosUsuario()
        obtenerDatosFacturacion()
        setupListeners()

        return view
    }

    //Cargar los datos del usuario
    private fun obtenerDatosUsuario() {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "preferencias_login",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val usuarioId = sharedPreferences.getString("usuario_id", "")
        Log.d("PerfilFragment", "Usuario ID recuperado: $usuarioId")

        if (usuarioId.isNullOrEmpty()) {
            Log.e("PerfilFragment", "Usuario ID no encontrado en SharedPreferences")
            Toast.makeText(
                requireContext(),
                "Usuario no encontrado. Por favor, inicia sesión nuevamente.",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            return
        }

        val url = "https://ladetechnologies.com/loveplan/obtener_perfil.php?usuario_id=$usuarioId"
        Log.d("PerfilFragment", "URL: $url")

        val requestQueue = Volley.newRequestQueue(requireContext())
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                Log.d("PerfilFragment", "Respuesta del servidor: $response")
                try {
                    val jsonObject = JSONObject(response)
                    val nombre = jsonObject.optString("Nombre", "Nombre no disponible")
                    val correo = jsonObject.optString("Correo", "Correo no disponible")
                    val novio = jsonObject.optString("Novio", "Novio no disponible")
                    val novia = jsonObject.optString("Novia", "Novia no disponible")
                    val fechaBoda = jsonObject.optString("FechaBoda", "Fecha no disponible")

                    // Actualizar vistas con los datos obtenidos
                    tvUsername.text = nombre
                    tvEmail.text = correo
                    tvNovio.text = "Novio: $novio"
                    tvNovia.text = "Novia: $novia"
                    tvFecha.text = "Fecha: $fechaBoda"

                    Log.d("PerfilFragment", "Nombre: $nombre, Correo: $correo, Novio: $novio, Novia: $novia, Fecha Boda: $fechaBoda")
                } catch (e: JSONException) {
                    Log.e("PerfilFragment", "Error al procesar los datos del usuario: ${e.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error al procesar los datos del servidor.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            { error ->
                Log.e("PerfilFragment", "Error en la conexión con el servidor: ${error.message}")
                Toast.makeText(
                    requireContext(),
                    "Error al conectar con el servidor.",
                    Toast.LENGTH_SHORT
                ).show()
            })

        requestQueue.add(stringRequest)
    }




    //Mostrar datos de facturacion
    private fun obtenerDatosFacturacion() {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "preferencias_login",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val usuarioId = sharedPreferences.getString("usuario_id", "")
        Log.d("FacturacionFragment", "Usuario ID recuperado: $usuarioId")

        if (usuarioId.isNullOrEmpty()) {
            Log.e("FacturacionFragment", "Usuario ID no encontrado en SharedPreferences")
            Toast.makeText(
                requireContext(),
                "Usuario no encontrado. Por favor, inicia sesión nuevamente.",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            return
        }

        val url = "https://ladetechnologies.com/loveplan/obtener_facturacion.php?idCliente=$usuarioId"
        Log.d("FacturacionFragment", "URL: $url")

        val requestQueue = Volley.newRequestQueue(requireContext())
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                Log.d("FacturacionFragment", "Respuesta del servidor: $response")
                try {
                    val jsonObject = JSONObject(response)
                    val status = jsonObject.optString("status", "error")
                    if (status == "success") {
                        val data = jsonObject.getJSONArray("data")
                        if (data.length() == 0) {
                            Toast.makeText(
                                requireContext(),
                                "No hay datos de facturación registrados.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@StringRequest
                        }

                        val facturacionList = mutableListOf<String>()
                        for (i in 0 until data.length()) {
                            val item = data.getJSONObject(i)
                            facturacionList.add(
                                "RFC: ${item.optString("rfc", "N/A")}\n" +
                                        "Nombre: ${item.optString("nombre", "N/A")}\n" +
                                        "Domicilio: ${item.optString("domicilio", "N/A")}\n" +
                                        "Código Postal: ${item.optString("codigo_postal", "N/A")}\n" +
                                        "Régimen Fiscal: ${item.optString("regimen_fiscal", "N/A")}"
                            )
                        }

                        // Actualiza el TextView con los datos obtenidos
                        tvBillingInfo.text = facturacionList.joinToString("\n\n")
                    } else {
                        val errorMessage = jsonObject.optString("message", "Error desconocido")
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Log.e("FacturacionFragment", "Error al procesar los datos de facturación: ${e.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error al procesar los datos del servidor.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            { error ->
                Log.e("FacturacionFragment", "Error en la conexión con el servidor: ${error.message}")
                Toast.makeText(
                    requireContext(),
                    "Error al conectar con el servidor.",
                    Toast.LENGTH_SHORT
                ).show()
            })

        requestQueue.add(stringRequest)
    }




    private fun setupListeners() {
        btnEditProfilePic.setOnClickListener {
            // Lógica para cambiar la foto de perfil
            val options = arrayOf("Seleccionar foto desde galería", "Tomar una foto")
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Cambiar foto de perfil")
            builder.setItems(options) { dialog, which ->
                when (which) {
                    0 -> openGallery()  // Abrir la galería
                    1 -> takePicture() // Tomar una foto
                }
            }
            builder.show()
        }

        cbEnableBilling.setOnCheckedChangeListener { _, isChecked ->
            setBillingFieldsEnabled(isChecked)
        }

        btnSubmitBilling.setOnClickListener {
            submitBillingInfo()
        }

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun setBillingFieldsEnabled(enabled: Boolean) {
        etRFC.isEnabled = enabled
        etBillingName.isEnabled = enabled
        etBillingAddress.isEnabled = enabled
        etBillingPostalCode.isEnabled = enabled
        etBillingFiscalRegime.isEnabled = enabled
        btnSubmitBilling.isEnabled = enabled
    }

    private fun logoutUser() {
        val sharedPreferences = getEncryptedSharedPreferences()
        sharedPreferences.edit().clear().apply()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun getEncryptedSharedPreferences(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "preferencias_login",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    //Enviar informacion a Facturacion
    private fun submitBillingInfo() {
        val rfc = etRFC.text.toString().trim()
        val nombre = etBillingName.text.toString().trim()
        val domicilio = etBillingAddress.text.toString().trim()
        val cp = etBillingPostalCode.text.toString().trim()
        val regimenfiscal = etBillingFiscalRegime.text.toString().trim()

        // Verificar si los campos están vacíos
        if (rfc.isEmpty() || nombre.isEmpty() || domicilio.isEmpty() || cp.isEmpty() || regimenfiscal.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener el `usuario_id` de las SharedPreferences
        val sharedPreferences = EncryptedSharedPreferences.create(
            "preferencias_login",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val usuarioId = sharedPreferences.getString("usuario_id", "")

        if (usuarioId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No se encontró el ID del usuario", Toast.LENGTH_SHORT).show()
            return
        }

        // Configurar los datos para enviar en la solicitud `POST`
        val params = HashMap<String, String>()
        params["idCliente"] = usuarioId
        params["rfc"] = rfc
        params["nombre"] = nombre
        params["domicilio"] = domicilio
        params["cp"] = cp
        params["regimenfiscal"] = regimenfiscal

        // URL del archivo PHP en el servidor
        val url = "https://ladetechnologies.com/loveplan/insertar_facturacion.php"

        // Configurar la solicitud `POST` usando Volley
        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                try {
                    val responseJson = JSONObject(response)
                    val status = responseJson.getString("status")
                    if (status == "success") {
                        val data = responseJson.getJSONObject("data")
                        val message = """
                    Datos guardados exitosamente:
                    ID: ${data.getInt("IdFacturacion")}
                    RFC: ${data.getString("rfc")}
                    Nombre: ${data.getString("nombre")}
                    Domicilio: ${data.getString("domicilio")}
                    CP: ${data.getInt("cp")}
                    Régimen Fiscal: ${data.getInt("regimenfiscal")}
                """.trimIndent()
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    } else {
                        val errorMessage = responseJson.getString("message")
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error al procesar la respuesta", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(requireContext(), "Error al conectar con el servidor: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

// Agregar la solicitud a la cola de Volley
        val requestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(stringRequest)
    }


    //Cambiar contraseña
    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null)
        val etNewPassword: EditText = dialogView.findViewById(R.id.etNewPassword)
        val etConfirmPassword: EditText = dialogView.findViewById(R.id.etConfirmPassword)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Cambiar contraseña")
            .setView(dialogView)
            .setPositiveButton("Aceptar") { _, _ ->
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(requireContext(), "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Enviar al servidor
                cambiarContrasena(newPassword)
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    // Método para cambiar la contraseña
    private fun cambiarContrasena(nuevaContrasena: String) {
        val sharedPreferences = getEncryptedSharedPreferences()
        val usuarioId = sharedPreferences.getString("usuario_id", "")

        if (usuarioId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "ID de usuario no encontrado. Por favor, inicia sesión.", Toast.LENGTH_SHORT).show()
            return
        }

        val url = "https://ladetechnologies.com/loveplan/cambiar_contrasena.php"
        val params = HashMap<String, String>()
        params["usuario_id"] = usuarioId
        params["nueva_contrasena"] = nuevaContrasena

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val responseJson = JSONObject(response)
                    val status = responseJson.getString("status")
                    if (status == "success") {
                        Toast.makeText(requireContext(), "Contraseña actualizada correctamente.", Toast.LENGTH_SHORT).show()
                    } else {
                        val mensaje = responseJson.getString("mensaje")
                        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error al procesar la respuesta.", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Error de conexión: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                return params
            }
        }

        val requestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(request)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, TAKE_PICTURE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val selectedImageUri: Uri? = data.data
                    selectedImageUri?.let {
                        // Actualizar el ImageView con la imagen seleccionada
                        profileImage.setImageURI(it)

                        // Si deseas guardar la imagen seleccionada internamente, guarda el URI o la imagen en SharedPreferences o en una base de datos
                        saveProfileImage(it)
                    }
                }
                TAKE_PICTURE_REQUEST -> {
                    val photo: Bitmap = data.extras?.get("data") as Bitmap
                    // Actualizar el ImageView con la foto tomada
                    profileImage.setImageBitmap(photo)

                    // Guardar la imagen tomada, por ejemplo, en el almacenamiento interno
                    saveProfileImage(photo)
                }
            }
        }
    }

    // Guarda la imagen seleccionada o tomada de forma interna (si es necesario)
    private fun saveProfileImage(imageUri: Uri) {
        // Guardar la imagen como archivo o en SharedPreferences, si es necesario
        // Ejemplo: guardando la URI de la imagen en SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("profile_image_uri", imageUri.toString()).apply()
    }

    private fun saveProfileImage(bitmap: Bitmap) {
        // Guardar la imagen como archivo o en SharedPreferences, si es necesario
        // Ejemplo: guardando la imagen en el almacenamiento interno
        val file = File(requireContext().filesDir, "profile_image.jpg")
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            // Guardar la ruta del archivo en SharedPreferences
            val sharedPreferences = requireContext().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("profile_image_path", file.absolutePath).apply()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}