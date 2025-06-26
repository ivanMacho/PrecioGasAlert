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

        // Cargar filtros actuales
        val filtros = EstacionManager.obtenerFiltros()
        tipoCombustibleInput.setText(filtros.tipoCombustible, false)
        findViewById<EditText>(R.id.input_precio_maximo).setText(filtros.precioMaximo?.toString() ?: "")
        findViewById<EditText>(R.id.input_distancia_maxima).setText(filtros.distanciaMaxima?.toString() ?: "")

        val btnAplicar = findViewById<Button>(R.id.btn_aplicar_filtros)
        btnAplicar.setOnClickListener {
            val tipo = tipoCombustibleInput.text.toString()
            val precio = findViewById<EditText>(R.id.input_precio_maximo).text.toString().toDoubleOrNull()
            val distancia = findViewById<EditText>(R.id.input_distancia_maxima).text.toString().toDoubleOrNull()
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