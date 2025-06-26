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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.ImageButton
import android.content.Intent

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val radioGroup = findViewById<RadioGroup>(R.id.radio_group_orden)
        val radioDistancia = findViewById<RadioButton>(R.id.radio_orden_distancia)
        val radioPrecio = findViewById<RadioButton>(R.id.radio_orden_precio)
        val btnAplicar = findViewById<Button>(R.id.btn_aplicar_filtros)
        val sliderFrecuencia = findViewById<com.google.android.material.slider.Slider>(R.id.slider_frecuencia)
        val textFrecuencia = findViewById<TextView>(R.id.text_frecuencia)
        val btnInfoFrecuencia = findViewById<ImageButton>(R.id.btn_info_frecuencia)
        val sliderDistancia = findViewById<com.google.android.material.slider.Slider>(R.id.slider_distancia)
        val textDistancia = findViewById<TextView>(R.id.text_distancia)
        val btnInfoDistancia = findViewById<ImageButton>(R.id.btn_info_distancia)

        // Cargar valor actual
        val filtros = EstacionManager.obtenerFiltros()
        if (filtros.orden == "precio") {
            radioPrecio.isChecked = true
        } else {
            radioDistancia.isChecked = true
        }

        // Cargar valores guardados (usamos SharedPreferences para estos parámetros)
        val prefs = getSharedPreferences("estaciones_prefs", MODE_PRIVATE)
        val frecuenciaActual = prefs.getInt("monitor_frecuencia", 10)
        val distanciaActual = prefs.getInt("monitor_distancia", 500)
        sliderFrecuencia.value = frecuenciaActual.toFloat()
        textFrecuencia.text = "Frecuencia de comprobación: $frecuenciaActual s"
        sliderDistancia.value = distanciaActual.toFloat()
        textDistancia.text = "Distancia mínima: $distanciaActual m"

        sliderFrecuencia.addOnChangeListener { _, value, _ ->
            textFrecuencia.text = "Frecuencia de comprobación: ${value.toInt()} s"
        }
        sliderDistancia.addOnChangeListener { _, value, _ ->
            textDistancia.text = "Distancia mínima: ${value.toInt()} m"
        }

        btnInfoFrecuencia.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Frecuencia de comprobación")
                .setMessage("Cada cuántos segundos el servicio de fondo comprobará si hay estaciones que cumplen tus filtros. Un valor bajo puede consumir más batería.")
                .setPositiveButton("Entendido", null)
                .show()
        }
        btnInfoDistancia.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Distancia mínima")
                .setMessage("Solo se comprobarán estaciones si te has movido al menos esta distancia (en metros) desde la última comprobación. Un valor bajo puede consumir más batería.")
                .setPositiveButton("Entendido", null)
                .show()
        }

        btnAplicar.setOnClickListener {
            val nuevoOrden = if (radioPrecio.isChecked) "precio" else "distancia"
            val nuevosFiltros = filtros.copy(orden = nuevoOrden)
            EstacionManager.guardarFiltros(this, nuevosFiltros)
            // Guardar frecuencia y distancia
            val nuevaFrecuencia = sliderFrecuencia.value.toInt()
            val nuevaDistancia = sliderDistancia.value.toInt()
            val frecuenciaAntes = prefs.getInt("monitor_frecuencia", 10)
            val distanciaAntes = prefs.getInt("monitor_distancia", 500)
            prefs.edit()
                .putInt("monitor_frecuencia", nuevaFrecuencia)
                .putInt("monitor_distancia", nuevaDistancia)
                .apply()
            val hanCambiado = (nuevaFrecuencia != frecuenciaAntes) || (nuevaDistancia != distanciaAntes)
            if (hanCambiado) {
                // Reiniciar el servicio
                val intent = Intent(this, LocationMonitorService::class.java)
                stopService(intent)
                startForegroundService(intent)
            }
            Toast.makeText(this, "Preferencias guardadas", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }
} 