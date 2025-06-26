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

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Lista de combustibles
        val combustibles = listOf(
            "Adblue",
            "Amoniaco",
            "Biodiesel",
            "Bioetanol",
            "Biogas Natural Comprimido",
            "Biogas Natural Licuado",
            "Diésel Renovable",
            "Gas Natural Comprimido",
            "Gas Natural Licuado",
            "Gases licuados del petróleo",
            "Gasoleo A",
            "Gasoleo B",
            "Gasoleo Premium",
            "Gasolina 95 E10",
            "Gasolina 95 E25",
            "Gasolina 95 E5",
            "Gasolina 95 E5 Premium",
            "Gasolina 95 E85",
            "Gasolina 98 E10",
            "Gasolina 98 E5",
            "Gasolina Renovable",
            "Hidrogeno",
            "Metanol"
        )

        val tipoCombustibleInput = findViewById<AutoCompleteTextView>(R.id.input_tipo_combustible)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, combustibles)
        tipoCombustibleInput.setAdapter(adapter)

        // Valores por defecto
        val defaultTipo = "Gasoleo A"
        val defaultPrecio = 1.8f
        val defaultDistancia = 5f

        // Cargar filtros actuales o usar por defecto
        val filtros = EstacionManager.obtenerFiltros()
        val tipoCombustible = if (filtros.tipoCombustible.isNotEmpty()) filtros.tipoCombustible else defaultTipo
        val precioMaximo = filtros.precioMaximo?.toFloat() ?: defaultPrecio
        val distanciaMaxima = filtros.distanciaMaxima?.toFloat() ?: defaultDistancia

        tipoCombustibleInput.setText(tipoCombustible, false)

        // Slider de precio
        val sliderPrecio = findViewById<Slider>(R.id.slider_precio_maximo)
        val textPrecio = findViewById<android.widget.TextView>(R.id.text_precio_maximo)
        sliderPrecio.value = precioMaximo
        textPrecio.text = "Precio máximo: %.2f €".format(precioMaximo)
        sliderPrecio.addOnChangeListener { _, value, _ ->
            textPrecio.text = "Precio máximo: %.2f €".format(value)
        }

        // Slider de distancia
        val sliderDistancia = findViewById<Slider>(R.id.slider_distancia_maxima)
        val textDistancia = findViewById<android.widget.TextView>(R.id.text_distancia_maxima)
        sliderDistancia.value = distanciaMaxima
        textDistancia.text = "Distancia máxima: %.0f km".format(distanciaMaxima)
        sliderDistancia.addOnChangeListener { _, value, _ ->
            textDistancia.text = "Distancia máxima: %.0f km".format(value)
        }

        val btnAplicar = findViewById<Button>(R.id.btn_aplicar_filtros)
        btnAplicar.setOnClickListener {
            val tipo = tipoCombustibleInput.text.toString()
            val precio = sliderPrecio.value.toDouble()
            val distancia = sliderDistancia.value.toDouble()
            val nuevosFiltros = EstacionFilter(tipo, precio, distancia)
            EstacionManager.guardarFiltros(this, nuevosFiltros)
            val resultIntent = Intent()
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    companion object {
        fun parsearEstacionFromJson(item: org.json.JSONObject): EstacionTerrestre {
            return EstacionTerrestre(
                id = item.optString("IDEESS"),
                rotulo = item.optString("Rótulo"),
                direccion = item.optString("Dirección"),
                localidad = item.optString("Localidad"),
                provincia = item.optString("Provincia"),
                municipio = item.optString("Municipio"),
                codigoPostal = item.optString("C.P."),
                horario = item.optString("Horario"),
                latitud = item.optString("Latitud"),
                longitud = item.optString("Longitud (WGS84)"),
                margen = item.optString("Margen"),
                tipoVenta = item.optString("Tipo Venta"),
                remision = item.optString("Remisión"),
                idMunicipio = item.optString("IDMunicipio"),
                idProvincia = item.optString("IDProvincia"),
                idCCAA = item.optString("IDCCAA"),
                precioGasolina95E5 = item.optString("Precio Gasolina 95 E5"),
                precioGasolina95E5Premium = item.optString("Precio Gasolina 95 E5 Premium"),
                precioGasolina95E10 = item.optString("Precio Gasolina 95 E10"),
                precioGasolina95E25 = item.optString("Precio Gasolina 95 E25"),
                precioGasolina95E85 = item.optString("Precio Gasolina 95 E85"),
                precioGasolina98E5 = item.optString("Precio Gasolina 98 E5"),
                precioGasolina98E10 = item.optString("Precio Gasolina 98 E10"),
                precioGasolinaRenovable = item.optString("Precio Gasolina Renovable"),
                precioGasoleoA = item.optString("Precio Gasoleo A"),
                precioGasoleoB = item.optString("Precio Gasoleo B"),
                precioGasoleoPremium = item.optString("Precio Gasoleo Premium"),
                precioDieselRenovable = item.optString("Precio Diésel Renovable"),
                precioBiodiesel = item.optString("Precio Biodiesel"),
                precioBioetanol = item.optString("Precio Bioetanol"),
                precioAdblue = item.optString("Precio Adblue"),
                precioGasNaturalComprimido = item.optString("Precio Gas Natural Comprimido"),
                precioGasNaturalLicuado = item.optString("Precio Gas Natural Licuado"),
                precioBiogasNaturalComprimido = item.optString("Precio Biogas Natural Comprimido"),
                precioBiogasNaturalLicuado = item.optString("Precio Biogas Natural Licuado"),
                precioGasesLicuadosPetroleo = item.optString("Precio Gases licuados del petróleo"),
                precioHidrogeno = item.optString("Precio Hidrogeno"),
                precioAmoniaco = item.optString("Precio Amoniaco"),
                precioMetanol = item.optString("Precio Metanol"),
                porcentajeBioEtanol = item.optString("% BioEtanol"),
                porcentajeEsterMetilico = item.optString("% Éster metílico")
            )
        }
    }
} 