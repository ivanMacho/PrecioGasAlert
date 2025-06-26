package com.machoapps.preciogasalert

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject

// Modelo de datos para una estación (estructura real del API)
data class EstacionTerrestre(
    // Información básica
    val id: String?,
    val rotulo: String?,
    val direccion: String?,
    val localidad: String?,
    val provincia: String?,
    val municipio: String?,
    val codigoPostal: String?,
    val horario: String?,
    
    // Coordenadas geográficas
    val latitud: String?,
    val longitud: String?,
    
    // Información adicional
    val margen: String?,
    val tipoVenta: String?,
    val remision: String?,
    
    // IDs de referencia
    val idMunicipio: String?,
    val idProvincia: String?,
    val idCCAA: String?,
    
    // Precios de combustibles
    val precioGasolina95E5: String?,
    val precioGasolina95E5Premium: String?,
    val precioGasolina95E10: String?,
    val precioGasolina95E25: String?,
    val precioGasolina95E85: String?,
    val precioGasolina98E5: String?,
    val precioGasolina98E10: String?,
    val precioGasolinaRenovable: String?,
    
    val precioGasoleoA: String?,
    val precioGasoleoB: String?,
    val precioGasoleoPremium: String?,
    val precioDieselRenovable: String?,
    
    val precioBiodiesel: String?,
    val precioBioetanol: String?,
    val precioAdblue: String?,
    
    val precioGasNaturalComprimido: String?,
    val precioGasNaturalLicuado: String?,
    val precioBiogasNaturalComprimido: String?,
    val precioBiogasNaturalLicuado: String?,
    val precioGasesLicuadosPetroleo: String?,
    
    val precioHidrogeno: String?,
    val precioAmoniaco: String?,
    val precioMetanol: String?,
    
    // Porcentajes de biocombustibles
    val porcentajeBioEtanol: String?,
    val porcentajeEsterMetilico: String?
)

// Modelo de respuesta completa del API
data class ApiResponse(
    val listaEESSPrecio: List<EstacionTerrestre>,
    val fecha: String?,
    val nota: String?,
    val resultadoConsulta: String?
)

object ApiService {
    private const val BASE_URL = "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/"
    private val client = OkHttpClient()

    fun obtenerEstaciones(): List<EstacionTerrestre> {
        val request = Request.Builder()
            .url(BASE_URL)
            .build()
        val estaciones = mutableListOf<EstacionTerrestre>()
        try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val json = JSONObject(body)
                    val lista = json.getJSONArray("ListaEESSPrecio")
                    for (i in 0 until lista.length()) {
                        val item = lista.getJSONObject(i)
                        estaciones.add(parsearEstacion(item))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Error al obtener estaciones", e)
        }
        return estaciones
    }

    fun obtenerEstacionesCompleto(): ApiResponse? {
        val request = Request.Builder()
            .url(BASE_URL)
            .build()
        try {
            val response: Response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (body != null) {
                    val json = JSONObject(body)
                    val lista = json.getJSONArray("ListaEESSPrecio")
                    val estaciones = mutableListOf<EstacionTerrestre>()
                    
                    for (i in 0 until lista.length()) {
                        val item = lista.getJSONObject(i)
                        estaciones.add(parsearEstacion(item))
                    }
                    
                    return ApiResponse(
                        listaEESSPrecio = estaciones,
                        fecha = json.optString("Fecha"),
                        nota = json.optString("Nota"),
                        resultadoConsulta = json.optString("ResultadoConsulta")
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("ApiService", "Error al obtener estaciones completo", e)
        }
        return null
    }
    
    private fun parsearEstacion(item: JSONObject): EstacionTerrestre {
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
} 