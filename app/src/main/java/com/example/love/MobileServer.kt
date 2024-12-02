package com.example.love

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import fi.iki.elonen.NanoHTTPD
import java.net.URLDecoder

/**
 * Clase que implementa un servidor embebido utilizando NanoHTTPD.
 * Este servidor escucha solicitudes POST enviadas desde el servidor web y genera notificaciones en el dispositivo.
 */
class MobileServer(private val context: Context, port: Int) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        return try {
            Log.d("MobileServer", "Solicitud recibida de: ${session.remoteIpAddress}")

            when (session.method) {
                Method.POST -> {
                    // Validar encabezado Content-Type
                    val contentType = session.headers["content-type"]
                    if (contentType != "application/x-www-form-urlencoded") {
                        Log.e("MobileServer", "Content-Type no válido: $contentType")
                        return newFixedLengthResponse(
                            Response.Status.UNSUPPORTED_MEDIA_TYPE,
                            "text/plain",
                            "Content-Type no soportado"
                        )
                    }

                    // Leer y decodificar el cuerpo de la solicitud POST
                    val postData = HashMap<String, String>()
                    val size = session.headers["content-length"]?.toInt() ?: 0
                    val buffer = ByteArray(size)
                    session.inputStream.read(buffer, 0, size)
                    val body = String(buffer)
                    body.split("&").forEach { pair ->
                        val keyValue = pair.split("=")
                        if (keyValue.size == 2) {
                            postData[keyValue[0]] = URLDecoder.decode(keyValue[1], "UTF-8")
                        }
                    }

                    // Log de los datos decodificados
                    Log.d("MobileServer", "Datos POST decodificados: $postData")

                    // Obtener el mensaje desde los datos decodificados
                    val message = postData["message"] ?: "Sin mensaje recibido"

                    // Log del mensaje recibido
                    Log.d("MobileServer", "Mensaje recibido: $message")

                    // Generar una notificación en el dispositivo
                    generarNotificacion(context, message)

                    // Retornar una respuesta al cliente
                    newFixedLengthResponse(Response.Status.OK, "text/plain", "Mensaje recibido: $message")
                }
                else -> {
                    // Manejo para métodos no soportados
                    Log.w("MobileServer", "Método HTTP no soportado: ${session.method}")
                    newFixedLengthResponse(
                        Response.Status.METHOD_NOT_ALLOWED,
                        "text/plain",
                        "Método no soportado"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MobileServer", "Error al procesar la solicitud: ${e.message}")
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error interno del servidor")
        }
    }

    private fun generarNotificacion(context: Context, mensaje: String) {
        val channelId = "notificaciones_loveplan"
        val channelName = "Notificaciones LovePlan"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Notificación LovePlan")
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}