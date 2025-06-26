package com.machoapps.preciogasalert

import org.json.JSONObject

/**
 * Modelo de filtros seleccionados por el usuario
 */
data class EstacionFilter(
    val tipoCombustible: String = "",
    val precioMaximo: Double? = null,
    val distanciaMaxima: Double? = null
) {
    fun toJson(): String {
        val obj = JSONObject()
        obj.put("tipoCombustible", tipoCombustible)
        obj.put("precioMaximo", precioMaximo)
        obj.put("distanciaMaxima", distanciaMaxima)
        return obj.toString()
    }

    companion object {
        fun fromJson(json: String?): EstacionFilter {
            if (json.isNullOrEmpty()) return EstacionFilter()
            return try {
                val obj = JSONObject(json)
                EstacionFilter(
                    tipoCombustible = obj.optString("tipoCombustible", ""),
                    precioMaximo = if (obj.isNull("precioMaximo")) null else obj.optDouble("precioMaximo"),
                    distanciaMaxima = if (obj.isNull("distanciaMaxima")) null else obj.optDouble("distanciaMaxima")
                )
            } catch (e: Exception) {
                EstacionFilter()
            }
        }
    }
} 