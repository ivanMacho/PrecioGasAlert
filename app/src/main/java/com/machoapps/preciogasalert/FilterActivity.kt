package com.machoapps.preciogasalert

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout

class FilterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        // Tipos de combustible disponibles
        val tiposCombustible = listOf(
            "Gasolina 95 E5", "Gasolina 95 E5 Premium", "Gasolina 95 E10", "Gasolina 95 E25", "Gasolina 95 E85",
            "Gasolina 98 E5", "Gasolina 98 E10", "Gasolina Renovable",
            "Gasoleo A", "Gasoleo B", "Gasoleo Premium", "Diésel Renovable",
            "Biodiesel", "Bioetanol", "Adblue",
            "Gas Natural Comprimido", "Gas Natural Licuado",
            "Biogas Natural Comprimido", "Biogas Natural Licuado",
            "Gases licuados del petróleo", "Hidrogeno", "Amoniaco", "Metanol"
        )

        val inputTipo = findViewById<AutoCompleteTextView>(R.id.input_tipo_combustible)
        val adapterTipo = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tiposCombustible)
        inputTipo.setAdapter(adapterTipo)

        val sliderPrecio = findViewById<Slider>(R.id.slider_precio_maximo)
        val textPrecio = findViewById<android.widget.TextView>(R.id.text_precio_maximo)
        val sliderDistancia = findViewById<Slider>(R.id.slider_distancia_maxima)
        val textDistancia = findViewById<android.widget.TextView>(R.id.text_distancia_maxima)
        val btnAplicar = findViewById<Button>(R.id.btn_aplicar_filtros)

        // Cargar valores actuales de los filtros
        val prefs = getSharedPreferences("estaciones_prefs", Context.MODE_PRIVATE)
        val filtros = EstacionManager.obtenerFiltros()
        inputTipo.setText(filtros.tipoCombustible, false)
        sliderPrecio.value = filtros.precioMaximo?.toFloat() ?: 1.8f
        textPrecio.text = "Precio máximo: %.2f €".format(sliderPrecio.value)
        sliderDistancia.value = filtros.distanciaMaxima?.toFloat() ?: 5f
        textDistancia.text = "Distancia máxima: %.0f km".format(sliderDistancia.value)

        sliderPrecio.addOnChangeListener { _, value, _ ->
            textPrecio.text = "Precio máximo: %.2f €".format(value)
        }
        sliderDistancia.addOnChangeListener { _, value, _ ->
            textDistancia.text = "Distancia máxima: %.0f km".format(value)
        }

        btnAplicar.setOnClickListener {
            val nuevoFiltro = EstacionFilter(
                tipoCombustible = inputTipo.text.toString(),
                precioMaximo = sliderPrecio.value.toDouble(),
                distanciaMaxima = sliderDistancia.value.toDouble()
            )
            EstacionManager.guardarFiltros(this, nuevoFiltro)
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    companion object {
        fun parsearEstacionFromJson(obj: org.json.JSONObject): EstacionTerrestre {
            return EstacionTerrestre(
                id = obj.optString("IDEESS"),
                rotulo = obj.optString("Rótulo"),
                direccion = obj.optString("Dirección"),
                localidad = obj.optString("Localidad"),
                provincia = obj.optString("Provincia"),
                municipio = obj.optString("Municipio"),
                codigoPostal = obj.optString("C.P."),
                horario = obj.optString("Horario"),
                latitud = obj.optString("Latitud"),
                longitud = obj.optString("Longitud (WGS84)"),
                margen = obj.optString("Margen"),
                tipoVenta = obj.optString("Tipo Venta"),
                remision = obj.optString("Remisión"),
                idMunicipio = obj.optString("IDMunicipio"),
                idProvincia = obj.optString("IDProvincia"),
                idCCAA = obj.optString("IDCCAA"),
                precioGasolina95E5 = obj.optString("Precio Gasolina 95 E5"),
                precioGasolina95E5Premium = obj.optString("Precio Gasolina 95 E5 Premium"),
                precioGasolina95E10 = obj.optString("Precio Gasolina 95 E10"),
                precioGasolina95E25 = obj.optString("Precio Gasolina 95 E25"),
                precioGasolina95E85 = obj.optString("Precio Gasolina 95 E85"),
                precioGasolina98E5 = obj.optString("Precio Gasolina 98 E5"),
                precioGasolina98E10 = obj.optString("Precio Gasolina 98 E10"),
                precioGasolinaRenovable = obj.optString("Precio Gasolina Renovable"),
                precioGasoleoA = obj.optString("Precio Gasoleo A"),
                precioGasoleoB = obj.optString("Precio Gasoleo B"),
                precioGasoleoPremium = obj.optString("Precio Gasoleo Premium"),
                precioDieselRenovable = obj.optString("Precio Diésel Renovable"),
                precioBiodiesel = obj.optString("Precio Biodiesel"),
                precioBioetanol = obj.optString("Precio Bioetanol"),
                precioAdblue = obj.optString("Precio Adblue"),
                precioGasNaturalComprimido = obj.optString("Precio Gas Natural Comprimido"),
                precioGasNaturalLicuado = obj.optString("Precio Gas Natural Licuado"),
                precioBiogasNaturalComprimido = obj.optString("Precio Biogas Natural Comprimido"),
                precioBiogasNaturalLicuado = obj.optString("Precio Biogas Natural Licuado"),
                precioGasesLicuadosPetroleo = obj.optString("Precio Gases licuados del petróleo"),
                precioHidrogeno = obj.optString("Precio Hidrogeno"),
                precioAmoniaco = obj.optString("Precio Amoniaco"),
                precioMetanol = obj.optString("Precio Metanol"),
                porcentajeBioEtanol = obj.optString("% BioEtanol"),
                porcentajeEsterMetilico = obj.optString("% Éster metílico")
            )
        }
    }
} 