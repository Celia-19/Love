package com.example.love

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Paquete(
    val idPaquete: Int,
    val nombre: String,
    val precio: Double,
    val imagenes: List<String>, // URLs o convertidos a URLs
    val beneficios: List<String>
) : Parcelable