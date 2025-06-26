package com.machoapps.preciogasalert

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gestor de estaciones de servicio
 * Utiliza el modelo de datos creado a partir del JSON de ejemplo
 */
object EstacionManager {
    
    private var estaciones: List<EstacionTerrestre> = emptyList()
    private var ultimaActualizacion: String? = null
    private var resultadoConsulta: String? = null
    private var filtros: EstacionFilter = EstacionFilter()
    
    private const val PREFS_NAME = "estaciones_prefs"
    private const val KEY_ESTACIONES = "estaciones_json"
    private const val KEY_FECHA = "fecha"
    private const val KEY_RESULTADO = "resultado"
    private const val KEY_FILTROS = "filtros"
    
    fun inicializar(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val estacionesJson = prefs.getString(KEY_ESTACIONES, null)
        val fecha = prefs.getString(KEY_FECHA, null)
        val resultado = prefs.getString(KEY_RESULTADO, null)
        val filtrosJson = prefs.getString(KEY_FILTROS, null)
        if (estacionesJson != null) {
            estaciones = JsonLoader.parsearListaEstaciones(estacionesJson)
        }
        ultimaActualizacion = fecha
        resultadoConsulta = resultado
        filtros = EstacionFilter.fromJson(filtrosJson)
    }
    
    fun guardarDatos(context: Context, apiResponse: ApiResponse) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ESTACIONES, JsonLoader.listaEstacionesToJson(apiResponse.listaEESSPrecio))
            .putString(KEY_FECHA, apiResponse.fecha)
            .putString(KEY_RESULTADO, apiResponse.resultadoConsulta)
            .apply()
        estaciones = apiResponse.listaEESSPrecio
        ultimaActualizacion = apiResponse.fecha
        resultadoConsulta = apiResponse.resultadoConsulta
    }
    
    fun guardarFiltros(context: Context, filtros: EstacionFilter) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FILTROS, filtros.toJson()).apply()
        this.filtros = filtros
    }
    
    fun obtenerFiltros(): EstacionFilter = filtros
    
    /**
     * Carga datos del API real usando corrutinas
     */
    suspend fun cargarDatosReales(context: Context, onSuccess: (ApiResponse) -> Unit, onError: (String) -> Unit) {
        try {
            val apiResponse = withContext(Dispatchers.IO) {
                ApiService.obtenerEstacionesCompleto()
            }
            
            if (apiResponse != null) {
                guardarDatos(context, apiResponse)
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
    
    fun obtenerEstacionesFiltradas(context: Context, userLat: Double? = null, userLon: Double? = null): List<EstacionTerrestre> {
        inicializar(context)
        return estaciones.filter { estacion ->
            val filtro = filtros
            val precio = when (filtro.tipoCombustible) {
                "Adblue" -> estacion.precioAdblue
                "Amoniaco" -> estacion.precioAmoniaco
                "Biodiesel" -> estacion.precioBiodiesel
                "Bioetanol" -> estacion.precioBioetanol
                "Biogas Natural Comprimido" -> estacion.precioBiogasNaturalComprimido
                "Biogas Natural Licuado" -> estacion.precioBiogasNaturalLicuado
                "Diésel Renovable" -> estacion.precioDieselRenovable
                "Gas Natural Comprimido" -> estacion.precioGasNaturalComprimido
                "Gas Natural Licuado" -> estacion.precioGasNaturalLicuado
                "Gases licuados del petróleo" -> estacion.precioGasesLicuadosPetroleo
                "Gasoleo A" -> estacion.precioGasoleoA
                "Gasoleo B" -> estacion.precioGasoleoB
                "Gasoleo Premium" -> estacion.precioGasoleoPremium
                "Gasolina 95 E10" -> estacion.precioGasolina95E10
                "Gasolina 95 E25" -> estacion.precioGasolina95E25
                "Gasolina 95 E5" -> estacion.precioGasolina95E5
                "Gasolina 95 E5 Premium" -> estacion.precioGasolina95E5Premium
                "Gasolina 95 E85" -> estacion.precioGasolina95E85
                "Gasolina 98 E10" -> estacion.precioGasolina98E10
                "Gasolina 98 E5" -> estacion.precioGasolina98E5
                "Gasolina Renovable" -> estacion.precioGasolinaRenovable
                "Hidrogeno" -> estacion.precioHidrogeno
                "Metanol" -> estacion.precioMetanol
                else -> null
            }
            val precioDouble = precio?.replace(",", ".")?.toDoubleOrNull()
            val cumplePrecio = filtro.precioMaximo == null || (precioDouble != null && precioDouble <= filtro.precioMaximo)
            // Filtrado por distancia
            val cumpleDistancia = if (filtro.distanciaMaxima != null && userLat != null && userLon != null) {
                val estLat = estacion.latitud?.replace(",", ".")?.toDoubleOrNull()
                val estLon = estacion.longitud?.replace(",", ".")?.toDoubleOrNull()
                if (estLat != null && estLon != null) {
                    val results = FloatArray(1)
                    android.location.Location.distanceBetween(userLat, userLon, estLat, estLon, results)
                    val distanciaKm = results[0] / 1000.0
                    distanciaKm <= filtro.distanciaMaxima
                } else {
                    false
                }
            } else true
            (filtro.tipoCombustible.isEmpty() || !precio.isNullOrEmpty()) && cumplePrecio && cumpleDistancia
        }
    }
} 