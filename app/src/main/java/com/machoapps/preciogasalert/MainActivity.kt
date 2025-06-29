package com.machoapps.preciogasalert

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.annotation.SuppressLint
import android.util.Log
import com.google.android.material.button.MaterialButton
import android.animation.ObjectAnimator
import android.widget.ImageButton
import android.view.Menu
import android.view.MenuItem
import com.machoapps.preciogasalert.FilterActivity
import com.machoapps.preciogasalert.SettingsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var textViewFecha: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EstacionAdapter
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val REQUEST_CONFIG = 1001
    private val REQUEST_LOCATION = 2001
    private val REQUEST_SETTINGS = 1002
    private var userLat: Double? = null
    private var userLon: Double? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isRefreshing = false
    private lateinit var fabRefrescar: FloatingActionButton
    private var refreshAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar datos y filtros en memoria al arrancar la app
        EstacionManager.inicializar(this)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.app_name)

        textViewFecha = findViewById(R.id.textViewFecha)
        recyclerView = findViewById(R.id.recyclerViewEstaciones)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EstacionAdapter(emptyList(), "", userLat, userLon)
        recyclerView.adapter = adapter

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val fabFilter = findViewById<FloatingActionButton>(R.id.fabFilter)
        fabFilter.setOnClickListener {
            val intent = Intent(this, FilterActivity::class.java)
            startActivityForResult(intent, REQUEST_CONFIG)
        }

        fabRefrescar = findViewById(R.id.fabRefrescar)
        fabRefrescar.setOnClickListener {
            if (!isRefreshing) {
                refrescarDatosManual()
            }
        }

        Log.d("PERMISOS", "Llamando a checkLocationPermissionAndLoad() desde onCreate")
        Toast.makeText(this, "Llamando a checkLocationPermissionAndLoad()", Toast.LENGTH_SHORT).show()
        // Pedir permisos de localización y mostrar datos
        checkLocationPermissionAndLoad()

        // Iniciar servicio de monitorización en segundo plano
        val serviceIntent = Intent(this, LocationMonitorService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun checkLocationPermissionAndLoad() {
        val tienePermiso = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        Log.d("PERMISOS", "¿Tiene permiso de localización? $tienePermiso")
        Toast.makeText(this, "¿Permiso localización? $tienePermiso", Toast.LENGTH_SHORT).show()
        if (!tienePermiso) {
            Log.d("PERMISOS", "Solicitando permiso de localización...")
            Toast.makeText(this, "Solicitando permiso de localización...", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION)
        } else {
            Log.d("PERMISOS", "Permiso ya concedido, obteniendo ubicación...")
            Toast.makeText(this, "Permiso ya concedido, obteniendo ubicación...", Toast.LENGTH_SHORT).show()
            obtenerUbicacionYMostrar()
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacionYMostrar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISOS", "No hay permiso en obtenerUbicacionYMostrar")
            Toast.makeText(this, "No hay permiso en obtenerUbicacionYMostrar", Toast.LENGTH_SHORT).show()
            userLat = null
            userLon = null
            mostrarDatosGuardadosYRefrescar()
            return
        }
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                userLat = location?.latitude
                userLon = location?.longitude
                Log.d("PERMISOS", "Ubicación obtenida: lat=$userLat lon=$userLon")
                Toast.makeText(this, "Ubicación: $userLat, $userLon", Toast.LENGTH_SHORT).show()
                mostrarDatosGuardadosYRefrescar()
            }.addOnFailureListener {
                Log.d("PERMISOS", "Error al obtener ubicación")
                Toast.makeText(this, "Error al obtener ubicación", Toast.LENGTH_SHORT).show()
                userLat = null
                userLon = null
                mostrarDatosGuardadosYRefrescar()
            }
        } catch (e: SecurityException) {
            Log.d("PERMISOS", "SecurityException al obtener ubicación")
            Toast.makeText(this, "SecurityException ubicación", Toast.LENGTH_SHORT).show()
            userLat = null
            userLon = null
            mostrarDatosGuardadosYRefrescar()
        }
    }

    private fun refrescarDatosManual() {
        iniciarAnimacionRefresco()
        coroutineScope.launch {
            EstacionManager.cargarDatosReales(
                context = this@MainActivity,
                onSuccess = {
                    actualizarUI()
                    detenerAnimacionRefresco()
                    Toast.makeText(this@MainActivity, "Datos actualizados", Toast.LENGTH_SHORT).show()
                },
                onError = {
                    detenerAnimacionRefresco()
                    Toast.makeText(this@MainActivity, "No se pudo actualizar: $it", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun iniciarAnimacionRefresco() {
        isRefreshing = true
        refreshAnimator = ObjectAnimator.ofFloat(fabRefrescar, "rotation", 0f, 360f).apply {
            duration = 800
            repeatCount = ObjectAnimator.INFINITE
            start()
        }
        fabRefrescar.isEnabled = false
    }

    private fun detenerAnimacionRefresco() {
        isRefreshing = false
        refreshAnimator?.cancel()
        fabRefrescar.rotation = 0f
        fabRefrescar.isEnabled = true
    }

    private fun mostrarDatosGuardadosYRefrescar() {
        actualizarUI()
        val prefs = getSharedPreferences("estaciones_prefs", MODE_PRIVATE)
        val fechaUltima = prefs.getString("fecha", null)
        val ahora = System.currentTimeMillis()
        var haceMasDe2h = true
        if (fechaUltima != null) {
            // Intentar parsear la fecha como timestamp, si no, como string tipo "2024-06-26 12:00:00"
            val timestamp = fechaUltima.toLongOrNull()
            if (timestamp != null) {
                haceMasDe2h = (ahora - timestamp) > 2 * 60 * 60 * 1000
            } else {
                // Si la fecha es un string, no podemos comparar bien, así que forzamos refresco cada vez
                haceMasDe2h = true
            }
        }
        if (!EstacionManager.tieneDatos() || haceMasDe2h) {
            iniciarAnimacionRefresco()
            coroutineScope.launch {
                EstacionManager.cargarDatosReales(
                    context = this@MainActivity,
                    onSuccess = {
                        actualizarUI()
                        detenerAnimacionRefresco()
                        Toast.makeText(this@MainActivity, "Datos actualizados", Toast.LENGTH_SHORT).show()
                        // Guardar timestamp actual como fecha
                        prefs.edit().putString("fecha", ahora.toString()).apply()
                    },
                    onError = {
                        detenerAnimacionRefresco()
                        Toast.makeText(this@MainActivity, "No se pudo actualizar: $it", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } else {
            detenerAnimacionRefresco()
        }
    }

    private fun actualizarUI() {
        val filtros = EstacionManager.obtenerFiltros()
        val chipTipo = findViewById<com.google.android.material.chip.Chip>(R.id.chipTipo)
        val chipPrecio = findViewById<com.google.android.material.chip.Chip>(R.id.chipPrecio)
        val chipDistancia = findViewById<com.google.android.material.chip.Chip>(R.id.chipDistancia)
        val chipTipoVenta = findViewById<com.google.android.material.chip.Chip>(R.id.chipTipoVenta)

        chipTipo.text = if (filtros.tipoCombustible.isNotEmpty()) filtros.tipoCombustible else "-"
        chipPrecio.text = if (filtros.precioMaximo != null) "≤ %.2f €".format(filtros.precioMaximo) else "-"
        chipDistancia.text = if (filtros.distanciaMaxima != null) "≤ %.1f km".format(filtros.distanciaMaxima) else "-"
        chipTipoVenta.text = when (filtros.tipoVenta) {
            "P" -> "Pública"
            "R" -> "Reservada"
            else -> "-"
        }

        val estaciones = EstacionManager.obtenerEstacionesFiltradas(this, userLat, userLon)
        adapter = EstacionAdapter(estaciones, filtros.tipoCombustible, userLat, userLon)
        recyclerView.adapter = adapter

        val fecha = EstacionManager.obtenerUltimaActualizacion()
        textViewFecha.text = "Precios del: " + (fecha?.let {
            val timestamp = it.toLongOrNull()
            if (timestamp != null) {
                // Formatear timestamp a fecha legible
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            } else {
                it
            }
        } ?: "-")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == REQUEST_CONFIG || requestCode == REQUEST_SETTINGS) && resultCode == Activity.RESULT_OK) {
            actualizarUI()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION) {
            Log.d("PERMISOS", "onRequestPermissionsResult: ${'$'}{grantResults.joinToString()}")
            Toast.makeText(this, "onRequestPermissionsResult: ${'$'}{grantResults.joinToString()}", Toast.LENGTH_SHORT).show()
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PERMISOS", "Permiso concedido por el usuario")
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()
                obtenerUbicacionYMostrar()
            } else {
                Log.d("PERMISOS", "Permiso denegado por el usuario")
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
                userLat = null
                userLon = null
                mostrarDatosGuardadosYRefrescar()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivityForResult(intent, REQUEST_SETTINGS)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val REQUEST_CONFIG = 1001
        private const val REQUEST_SETTINGS = 1002
    }
} 