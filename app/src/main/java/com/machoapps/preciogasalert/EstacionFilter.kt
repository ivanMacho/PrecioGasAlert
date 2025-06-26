package com.machoapps.preciogasalert

import org.json.JSONObject

/**
 * Modelo de filtros seleccionados por el usuario
 */
data class EstacionFilter(
    val tipoCombustible: String = "",
    val precioMaximo: Double? = null,
    val distanciaMaxima: Double? = null,
    val orden: String = "distancia" // "distancia" o "precio"
) {
    fun toJson(): String {
        val obj = JSONObject()
        obj.put("tipoCombustible", tipoCombustible)
        obj.put("precioMaximo", precioMaximo)
        obj.put("distanciaMaxima", distanciaMaxima)
        obj.put("orden", orden)
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
                    distanciaMaxima = if (obj.isNull("distanciaMaxima")) null else obj.optDouble("distanciaMaxima"),
                    orden = obj.optString("orden", "distancia")
                )
            } catch (e: Exception) {
                EstacionFilter()
            }
        }
    }
} 