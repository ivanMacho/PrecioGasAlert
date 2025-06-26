package com.machoapps.preciogasalert

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object JsonLoader {
    
    /**
     * Carga el archivo JSON de ejemplo desde los recursos
     */
    fun cargarEstacionesEjemplo(context: Context): ApiResponse? {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.estaciones_ejemplo)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()
            
            parsearJsonEjemplo(jsonString)
        } catch (e: Exception) {
            Log.e("JsonLoader", "Error al cargar JSON de ejemplo", e)
            null
        }
    }
    
    /**
     * Parsea el JSON de ejemplo y lo convierte en objetos de datos
     */
    private fun parsearJsonEjemplo(jsonString: String): ApiResponse? {
        return try {
            val json = JSONObject(jsonString)
            val lista = json.getJSONArray("ListaEESSPrecio")
            val estaciones = mutableListOf<EstacionTerrestre>()
            
            for (i in 0 until lista.length()) {
                val item = lista.getJSONObject(i)
                estaciones.add(parsearEstacionEjemplo(item))
            }
            
            ApiResponse(
                listaEESSPrecio = estaciones,
                fecha = json.optString("Fecha"),
                nota = json.optString("Nota"),
                resultadoConsulta = json.optString("ResultadoConsulta")
            )
        } catch (e: Exception) {
            Log.e("JsonLoader", "Error al parsear JSON de ejemplo", e)
            null
        }
    }
    
    /**
     * Parsea una estación individual del JSON de ejemplo
     */
    private fun parsearEstacionEjemplo(item: JSONObject): EstacionTerrestre {
        return EstacionTerrestre(
            // Información básica
            id = item.optString("IDEESS"),
            rotulo = item.optString("Rótulo"),
            direccion = item.optString("Dirección"),
            localidad = item.optString("Localidad"),
            provincia = item.optString("Provincia"),
            municipio = item.optString("Municipio"),
            codigoPostal = item.optString("C.P."),
            horario = item.optString("Horario"),
            
            // Coordenadas geográficas
            latitud = item.optString("Latitud"),
            longitud = item.optString("Longitud (WGS84)"),
            
            // Información adicional
            margen = item.optString("Margen"),
            tipoVenta = item.optString("Tipo Venta"),
            remision = item.optString("Remisión"),
            
            // IDs de referencia
            idMunicipio = item.optString("IDMunicipio"),
            idProvincia = item.optString("IDProvincia"),
            idCCAA = item.optString("IDCCAA"),
            
            // Precios de gasolinas
            precioGasolina95E5 = item.optString("Precio Gasolina 95 E5"),
            precioGasolina95E5Premium = item.optString("Precio Gasolina 95 E5 Premium"),
            precioGasolina95E10 = item.optString("Precio Gasolina 95 E10"),
            precioGasolina95E25 = item.optString("Precio Gasolina 95 E25"),
            precioGasolina95E85 = item.optString("Precio Gasolina 95 E85"),
            precioGasolina98E5 = item.optString("Precio Gasolina 98 E5"),
            precioGasolina98E10 = item.optString("Precio Gasolina 98 E10"),
            precioGasolinaRenovable = item.optString("Precio Gasolina Renovable"),
            
            // Precios de gasóleos
            precioGasoleoA = item.optString("Precio Gasoleo A"),
            precioGasoleoB = item.optString("Precio Gasoleo B"),
            precioGasoleoPremium = item.optString("Precio Gasoleo Premium"),
            precioDieselRenovable = item.optString("Precio Diésel Renovable"),
            
            // Precios de biocombustibles
            precioBiodiesel = item.optString("Precio Biodiesel"),
            precioBioetanol = item.optString("Precio Bioetanol"),
            precioAdblue = item.optString("Precio Adblue"),
            
            // Precios de gases
            precioGasNaturalComprimido = item.optString("Precio Gas Natural Comprimido"),
            precioGasNaturalLicuado = item.optString("Precio Gas Natural Licuado"),
            precioBiogasNaturalComprimido = item.optString("Precio Biogas Natural Comprimido"),
            precioBiogasNaturalLicuado = item.optString("Precio Biogas Natural Licuado"),
            precioGasesLicuadosPetroleo = item.optString("Precio Gases licuados del petróleo"),
            
            // Otros combustibles
            precioHidrogeno = item.optString("Precio Hidrogeno"),
            precioAmoniaco = item.optString("Precio Amoniaco"),
            precioMetanol = item.optString("Precio Metanol"),
            
            // Porcentajes de biocombustibles
            porcentajeBioEtanol = item.optString("% BioEtanol"),
            porcentajeEsterMetilico = item.optString("% Éster metílico")
        )
    }
    
    /**
     * Obtiene estadísticas básicas de los datos de ejemplo
     */
    fun obtenerEstadisticasEjemplo(context: Context): String {
        val apiResponse = cargarEstacionesEjemplo(context)
        return if (apiResponse != null) {
            val estaciones = apiResponse.listaEESSPrecio
            val totalEstaciones = estaciones.size
            val provincias = estaciones.mapNotNull { it.provincia }.distinct()
            val rotulos = estaciones.mapNotNull { it.rotulo }.distinct()
            val municipios = estaciones.mapNotNull { it.municipio }.distinct()
            
            // Calcular precios promedio
            val preciosGasolina95 = estaciones.mapNotNull { it.precioGasolina95E5 }
                .filter { it.isNotEmpty() }
                .mapNotNull { it.replace(",", ".").toDoubleOrNull() }
            
            val preciosGasoleoA = estaciones.mapNotNull { it.precioGasoleoA }
                .filter { it.isNotEmpty() }
                .mapNotNull { it.replace(",", ".").toDoubleOrNull() }
            
            val precioPromedioGasolina95 = if (preciosGasolina95.isNotEmpty()) {
                preciosGasolina95.average()
            } else null
            
            val precioPromedioGasoleoA = if (preciosGasoleoA.isNotEmpty()) {
                preciosGasoleoA.average()
            } else null
            
            """
            ESTADÍSTICAS DE EJEMPLO:
            =======================
            Total de estaciones: $totalEstaciones
            Provincias: ${provincias.joinToString(", ")}
            Municipios: ${municipios.joinToString(", ")}
            Marcas: ${rotulos.joinToString(", ")}
            Fecha: ${apiResponse.fecha}
            Resultado: ${apiResponse.resultadoConsulta}
            
            PRECIOS PROMEDIO:
            Gasolina 95 E5: ${precioPromedioGasolina95?.let { "%.3f".format(it) } ?: "N/A"} €
            Gasóleo A: ${precioPromedioGasoleoA?.let { "%.3f".format(it) } ?: "N/A"} €
            """.trimIndent()
        } else {
            "Error al cargar datos de ejemplo"
        }
    }

    fun listaEstacionesToJson(lista: List<EstacionTerrestre>): String {
        val arr = JSONArray()
        lista.forEach { est ->
            val obj = JSONObject()
            obj.put("IDEESS", est.id)
            obj.put("Rótulo", est.rotulo)
            obj.put("Dirección", est.direccion)
            obj.put("Localidad", est.localidad)
            obj.put("Provincia", est.provincia)
            obj.put("Municipio", est.municipio)
            obj.put("C.P.", est.codigoPostal)
            obj.put("Horario", est.horario)
            obj.put("Latitud", est.latitud)
            obj.put("Longitud (WGS84)", est.longitud)
            obj.put("Margen", est.margen)
            obj.put("Tipo Venta", est.tipoVenta)
            obj.put("Remisión", est.remision)
            obj.put("IDMunicipio", est.idMunicipio)
            obj.put("IDProvincia", est.idProvincia)
            obj.put("IDCCAA", est.idCCAA)
            obj.put("Precio Gasolina 95 E5", est.precioGasolina95E5)
            obj.put("Precio Gasolina 95 E5 Premium", est.precioGasolina95E5Premium)
            obj.put("Precio Gasolina 95 E10", est.precioGasolina95E10)
            obj.put("Precio Gasolina 95 E25", est.precioGasolina95E25)
            obj.put("Precio Gasolina 95 E85", est.precioGasolina95E85)
            obj.put("Precio Gasolina 98 E5", est.precioGasolina98E5)
            obj.put("Precio Gasolina 98 E10", est.precioGasolina98E10)
            obj.put("Precio Gasolina Renovable", est.precioGasolinaRenovable)
            obj.put("Precio Gasoleo A", est.precioGasoleoA)
            obj.put("Precio Gasoleo B", est.precioGasoleoB)
            obj.put("Precio Gasoleo Premium", est.precioGasoleoPremium)
            obj.put("Precio Diésel Renovable", est.precioDieselRenovable)
            obj.put("Precio Biodiesel", est.precioBiodiesel)
            obj.put("Precio Bioetanol", est.precioBioetanol)
            obj.put("Precio Adblue", est.precioAdblue)
            obj.put("Precio Gas Natural Comprimido", est.precioGasNaturalComprimido)
            obj.put("Precio Gas Natural Licuado", est.precioGasNaturalLicuado)
            obj.put("Precio Biogas Natural Comprimido", est.precioBiogasNaturalComprimido)
            obj.put("Precio Biogas Natural Licuado", est.precioBiogasNaturalLicuado)
            obj.put("Precio Gases licuados del petróleo", est.precioGasesLicuadosPetroleo)
            obj.put("Precio Hidrogeno", est.precioHidrogeno)
            obj.put("Precio Amoniaco", est.precioAmoniaco)
            obj.put("Precio Metanol", est.precioMetanol)
            obj.put("% BioEtanol", est.porcentajeBioEtanol)
            obj.put("% Éster metílico", est.porcentajeEsterMetilico)
            arr.put(obj)
        }
        return arr.toString()
    }

    fun parsearListaEstaciones(json: String): List<EstacionTerrestre> {
        val arr = JSONArray(json)
        val lista = mutableListOf<EstacionTerrestre>()
        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            lista.add(SettingsActivity.parsearEstacionFromJson(item))
        }
        return lista
    }
} 