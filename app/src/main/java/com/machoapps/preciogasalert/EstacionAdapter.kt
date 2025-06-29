package com.machoapps.preciogasalert

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EstacionAdapter(
    private var estaciones: List<EstacionTerrestre>,
    private val tipoCombustible: String,
    private val userLat: Double?,
    private val userLon: Double?
) : RecyclerView.Adapter<EstacionAdapter.EstacionViewHolder>() {

    class EstacionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textNombre: TextView = view.findViewById(R.id.textNombreEstacion)
        val textPrecio: TextView = view.findViewById(R.id.textPrecioEstacion)
        val textDistancia: TextView = view.findViewById(R.id.textDistanciaEstacion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_estacion, parent, false)
        return EstacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstacionViewHolder, position: Int) {
        val estacion = estaciones[position]
        holder.textNombre.text = estacion.rotulo ?: "-"
        val precio = when (tipoCombustible) {
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
        holder.textPrecio.text = if (!precio.isNullOrEmpty()) "$precio €" else "-"

        // Calcular y mostrar distancia
        val estLat = estacion.latitud?.replace(",", ".")?.toDoubleOrNull()
        val estLon = estacion.longitud?.replace(",", ".")?.toDoubleOrNull()
        if (userLat != null && userLon != null && estLat != null && estLon != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(userLat, userLon, estLat, estLon, results)
            val distancia = results[0]
            holder.textDistancia.text = if (distancia > 1000) "%.1f km".format(distancia/1000) else "%.0f m".format(distancia)
        } else {
            holder.textDistancia.text = "-"
        }

        // Click para abrir Google Maps
        holder.itemView.setOnClickListener {
            if (estLat != null && estLon != null) {
                val uri = Uri.parse("geo:$estLat,$estLon?q=$estLat,$estLon(${estacion.rotulo ?: "Estación"})")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = estaciones.size

    fun updateData(newEstaciones: List<EstacionTerrestre>, nuevoTipo: String) {
        estaciones = newEstaciones
        // Si el tipo de combustible cambia, actualiza también
        // (esto fuerza el refresco de precios)
        notifyDataSetChanged()
    }
} 