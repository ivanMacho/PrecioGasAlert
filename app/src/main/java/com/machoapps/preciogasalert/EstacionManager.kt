package com.machoapps.preciogasalert

import android.content.Context
import android.util.Log

/**
 * Gestor de estaciones de servicio
 * Utiliza el modelo de datos creado a partir del JSON de ejemplo
 */
object EstacionManager {
    
    private var estaciones: List<EstacionTerrestre> = emptyList()
    private var ultimaActualizacion: String? = null
    private var resultadoConsulta: String? = null
    
    /**
     * Carga datos del API real
     */
    fun cargarDatosReales(onSuccess: (ApiResponse) -> Unit, onError: (String) -> Unit) {
        try {
            val apiResponse = ApiService.obtenerEstacionesCompleto()
            if (apiResponse != null) {
                estaciones = apiResponse.listaEESSPrecio
                ultimaActualizacion = apiResponse.fecha
                resultadoConsulta = apiResponse.resultadoConsulta
                onSuccess(apiResponse)
            } else {
                onError("No se pudieron cargar los datos del API")
            }
        } catch (e: Exception) {
            Log.e("EstacionManager", "Error al cargar datos reales", e)
            onError("Error de conexión: ${e.message}")
        }
    }
    
    /**
     * Obtiene todas las estaciones cargadas
     */
    fun obtenerEstaciones(): List<EstacionTerrestre> = estaciones
    
    /**
     * Busca estaciones por provincia
     */
    fun buscarPorProvincia(provincia: String): List<EstacionTerrestre> {
        return estaciones.filter { 
            it.provincia?.contains(provincia, ignoreCase = true) == true 
        }
    }
    
    /**
     * Busca estaciones por municipio
     */
    fun buscarPorMunicipio(municipio: String): List<EstacionTerrestre> {
        return estaciones.filter { 
            it.municipio?.contains(municipio, ignoreCase = true) == true 
        }
    }
    
    /**
     * Busca estaciones por marca (rótulo)
     */
    fun buscarPorMarca(marca: String): List<EstacionTerrestre> {
        return estaciones.filter { 
            it.rotulo?.contains(marca, ignoreCase = true) == true 
        }
    }
    
    /**
     * Obtiene las estaciones con el precio más bajo de un combustible específico
     */
    fun obtenerMasBaratas(combustible: String, cantidad: Int = 5): List<EstacionTerrestre> {
        val estacionesConPrecio = estaciones.filter { estacion ->
            when (combustible.lowercase()) {
                "gasolina 95" -> !estacion.precioGasolina95E5.isNullOrEmpty()
                "gasoleo a" -> !estacion.precioGasoleoA.isNullOrEmpty()
                "gasoleo b" -> !estacion.precioGasoleoB.isNullOrEmpty()
                "gasolina 98" -> !estacion.precioGasolina98E5.isNullOrEmpty()
                else -> false
            }
        }
        
        return estacionesConPrecio.sortedBy { estacion ->
            when (combustible.lowercase()) {
                "gasolina 95" -> estacion.precioGasolina95E5?.replace(",", ".")?.toDoubleOrNull() ?: Double.MAX_VALUE
                "gasoleo a" -> estacion.precioGasoleoA?.replace(",", ".")?.toDoubleOrNull() ?: Double.MAX_VALUE
                "gasoleo b" -> estacion.precioGasoleoB?.replace(",", ".")?.toDoubleOrNull() ?: Double.MAX_VALUE
                "gasolina 98" -> estacion.precioGasolina98E5?.replace(",", ".")?.toDoubleOrNull() ?: Double.MAX_VALUE
                else -> Double.MAX_VALUE
            }
        }.take(cantidad)
    }
    
    /**
     * Obtiene estadísticas generales
     */
    fun obtenerEstadisticas(): String {
        if (estaciones.isEmpty()) {
            return "No hay datos cargados"
        }
        
        val totalEstaciones = estaciones.size
        val provincias = estaciones.mapNotNull { it.provincia }.distinct()
        val municipios = estaciones.mapNotNull { it.municipio }.distinct()
        val marcas = estaciones.mapNotNull { it.rotulo }.distinct()
        
        return """
            ESTADÍSTICAS GENERALES:
            =======================
            Total de estaciones: $totalEstaciones
            Provincias: ${provincias.size}
            Municipios: ${municipios.size}
            Marcas: ${marcas.size}
            Última actualización: ${ultimaActualizacion ?: "N/A"}
            Estado: ${resultadoConsulta ?: "N/A"}
        """.trimIndent()
    }
    
    /**
     * Verifica si hay datos cargados
     */
    fun tieneDatos(): Boolean = estaciones.isNotEmpty()
    
    /**
     * Obtiene la última fecha de actualización
     */
    fun obtenerUltimaActualizacion(): String? = ultimaActualizacion
    
    /**
     * Obtiene el resultado de la última consulta
     */
    fun obtenerResultadoConsulta(): String? = resultadoConsulta
} 