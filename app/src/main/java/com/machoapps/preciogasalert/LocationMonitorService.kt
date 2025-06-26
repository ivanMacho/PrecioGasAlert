package com.machoapps.preciogasalert

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*

class LocationMonitorService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var lastNotifiedStations: Set<String> = emptySet()
    private val CHANNEL_ID = "monitor_gas_channel"
    private val NOTIF_ID = 101

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Monitorizando ubicación..."))
        setupLocationUpdates()
    }

    private fun setupLocationUpdates() {
        val prefs = getSharedPreferences("estaciones_prefs", Context.MODE_PRIVATE)
        val frecuencia = prefs.getInt("monitor_frecuencia", 10) * 1000L // en ms
        val distancia = prefs.getInt("monitor_distancia", 500).toFloat() // en metros
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, frecuencia)
            .setMinUpdateDistanceMeters(distancia)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    checkStations(location)
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        }
    }

    private fun checkStations(location: Location) {
        val estaciones = EstacionManager.obtenerEstacionesFiltradas(this, location.latitude, location.longitude)
        if (estaciones.isNotEmpty()) {
            lanzarNotificacionResumen(estaciones, location)
            lastNotifiedStations = estaciones.mapNotNull { it.id }.toSet()
        }
    }

    private fun lanzarNotificacionResumen(estaciones: List<EstacionTerrestre>, userLocation: Location) {
        val filtros = EstacionManager.obtenerFiltros()
        val masCercana = estaciones.minByOrNull { estacion ->
            val lat = estacion.latitud?.replace(",", ".")?.toDoubleOrNull()
            val lon = estacion.longitud?.replace(",", ".")?.toDoubleOrNull()
            if (lat != null && lon != null) {
                val results = FloatArray(1)
                Location.distanceBetween(userLocation.latitude, userLocation.longitude, lat, lon, results)
                results[0]
            } else {
                Float.MAX_VALUE
            }
        }
        if (masCercana != null) {
            val lat = masCercana.latitud?.replace(",", ".")?.toDoubleOrNull()
            val lon = masCercana.longitud?.replace(",", ".")?.toDoubleOrNull()
            val results = FloatArray(1)
            if (lat != null && lon != null) {
                Location.distanceBetween(userLocation.latitude, userLocation.longitude, lat, lon, results)
            }
            val distancia = if (results[0] > 1000) "%.1f km".format(results[0]/1000) else "%.0f m".format(results[0])
            val precio = when (filtros.tipoCombustible) {
                "Adblue" -> masCercana.precioAdblue
                "Amoniaco" -> masCercana.precioAmoniaco
                "Biodiesel" -> masCercana.precioBiodiesel
                "Bioetanol" -> masCercana.precioBioetanol
                "Biogas Natural Comprimido" -> masCercana.precioBiogasNaturalComprimido
                "Biogas Natural Licuado" -> masCercana.precioBiogasNaturalLicuado
                "Diésel Renovable" -> masCercana.precioDieselRenovable
                "Gas Natural Comprimido" -> masCercana.precioGasNaturalComprimido
                "Gas Natural Licuado" -> masCercana.precioGasNaturalLicuado
                "Gases licuados del petróleo" -> masCercana.precioGasesLicuadosPetroleo
                "Gasoleo A" -> masCercana.precioGasoleoA
                "Gasoleo B" -> masCercana.precioGasoleoB
                "Gasoleo Premium" -> masCercana.precioGasoleoPremium
                "Gasolina 95 E10" -> masCercana.precioGasolina95E10
                "Gasolina 95 E25" -> masCercana.precioGasolina95E25
                "Gasolina 95 E5" -> masCercana.precioGasolina95E5
                "Gasolina 95 E5 Premium" -> masCercana.precioGasolina95E5Premium
                "Gasolina 95 E85" -> masCercana.precioGasolina95E85
                "Gasolina 98 E10" -> masCercana.precioGasolina98E10
                "Gasolina 98 E5" -> masCercana.precioGasolina98E5
                "Gasolina Renovable" -> masCercana.precioGasolinaRenovable
                "Hidrogeno" -> masCercana.precioHidrogeno
                "Metanol" -> masCercana.precioMetanol
                else -> null
            }
            val textoPrecio = if (!precio.isNullOrEmpty()) "$precio €/L" else "-"
            val texto = "${masCercana.rotulo ?: "Estación"} - $textoPrecio a $distancia" +
                if (estaciones.size > 1) ". Y ${estaciones.size - 1} más cumplen tus filtros." else ""
            val intent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val notif = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_gas_station)
                .setContentTitle("¡Hay estaciones que cumplen tus filtros!")
                .setContentText(texto)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIF_ID, notif)
        }
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_gas_station)
            .setContentTitle("Precio Gas Alert")
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Monitorización Gas", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
} 