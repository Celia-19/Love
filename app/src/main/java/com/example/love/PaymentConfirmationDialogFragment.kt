package com.example.love

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class PaymentConfirmationDialogFragment : DialogFragment() {

    private lateinit var tvPaqueteNombre: TextView
    private lateinit var tvPaquetePrecio: TextView
    private lateinit var tvMontoAnticipo: TextView
    private lateinit var btnConfirmarPago: Button
    private lateinit var btnCancelar: Button

    companion object {
        private const val ARG_PAQUETE_NOMBRE = "paquete_nombre"
        private const val ARG_PAQUETE_PRECIO = "paquete_precio"
        private const val ARG_CARD_NUMBER = "card_number"

        fun newInstance(paqueteNombre: String, paquetePrecio: Double, cardNumber: String): PaymentConfirmationDialogFragment {
            val fragment = PaymentConfirmationDialogFragment()
            val args = Bundle()
            args.putString(ARG_PAQUETE_NOMBRE, paqueteNombre)
            args.putDouble(ARG_PAQUETE_PRECIO, paquetePrecio)
            args.putString(ARG_CARD_NUMBER, cardNumber)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_confirmation_dialog, container, false)

        // Referencias a los elementos de la vista
        tvPaqueteNombre = view.findViewById(R.id.tvPaqueteNombre)
        tvPaquetePrecio = view.findViewById(R.id.tvPaquetePrecio)
        tvMontoAnticipo = view.findViewById(R.id.tvMontoAnticipo)
        btnConfirmarPago = view.findViewById(R.id.btnConfirmarPago)
        btnCancelar = view.findViewById(R.id.btnCancelar)

        // Obtener los datos del paquete y monto
        val paqueteNombre = arguments?.getString(ARG_PAQUETE_NOMBRE) ?: "Paquete desconocido"
        val paquetePrecio = arguments?.getDouble(ARG_PAQUETE_PRECIO) ?: 0.0
        val cardNumber = arguments?.getString(ARG_CARD_NUMBER) ?: "XXXX-XXXX-XXXX"

        // Calcular el monto del anticipo
        val montoAnticipo = paquetePrecio * 0.15

        // Mostrar los detalles en la vista
        tvPaqueteNombre.text = paqueteNombre
        tvPaquetePrecio.text = "Precio: $$paquetePrecio"
        tvMontoAnticipo.text = "Monto a Pagar: $$montoAnticipo"

        // Configurar los botones
        btnConfirmarPago.setOnClickListener {
            val cardNumber = arguments?.getString("card_number")
            val intent = Intent(requireContext(), ConfirmacionActivity::class.java)
            intent.putExtra("cardNumber", cardNumber)
            startActivity(intent)
            dismiss()  // Cerrar el diálogo
        }

        btnCancelar.setOnClickListener {
            dismiss()  // Cerrar el diálogo si el usuario cancela
        }

        return view
    }
}