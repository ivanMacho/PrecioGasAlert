package com.machoapps.preciogasalert

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.slider.Slider
import android.widget.AutoCompleteTextView
import android.widget.TextView

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val radioGroup = findViewById<RadioGroup>(R.id.radio_group_orden)
        val radioDistancia = findViewById<RadioButton>(R.id.radio_orden_distancia)
        val radioPrecio = findViewById<RadioButton>(R.id.radio_orden_precio)
        val btnAplicar = findViewById<Button>(R.id.btn_aplicar_filtros)

        // Cargar valor actual
        val filtros = EstacionManager.obtenerFiltros()
        if (filtros.orden == "precio") {
            radioPrecio.isChecked = true
        } else {
            radioDistancia.isChecked = true
        }

        btnAplicar.setOnClickListener {
            val nuevoOrden = if (radioPrecio.isChecked) "precio" else "distancia"
            val nuevosFiltros = filtros.copy(orden = nuevoOrden)
            EstacionManager.guardarFiltros(this, nuevosFiltros)
            Toast.makeText(this, "Preferencia de orden guardada", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }
} 