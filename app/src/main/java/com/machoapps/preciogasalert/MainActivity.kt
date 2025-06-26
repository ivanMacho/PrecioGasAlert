package com.machoapps.preciogasalert

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var textViewWelcome: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        textViewWelcome = findViewById(R.id.textViewWelcome)
        
        // Configurar interfaz principal
        configurarInterfaz()
        
        // Configurar click listener para cargar datos
        textViewWelcome.setOnClickListener {
            cargarDatosReales()
        }
    }
    
    private fun configurarInterfaz() {
        val infoText = """
            PRECIO GAS ALERT
            =================
            
            SISTEMA DE MONITOREO DE PRECIOS
            ESTACIONES TERRESTRES
            
            MODELO DE DATOS CREADO:
            ✓ Estaciones de servicio
            ✓ Precios de combustibles
            ✓ Información geográfica
            ✓ Metadatos de consulta
            
            LISTO PARA CONECTAR CON API
            ===========================
            
            TOCA PARA CARGAR DATOS REALES
        """.trimIndent()
        
        textViewWelcome.text = infoText
    }
    
    private fun cargarDatosReales() {
        // Mostrar mensaje de carga
        textViewWelcome.text = """
            PRECIO GAS ALERT
            =================
            
            CARGANDO DATOS DEL API...
            =========================
            
            Conectando con Ministerio de Industria...
            Obteniendo precios actualizados...
            
            Por favor espera...
        """.trimIndent()
        
        // Cargar datos reales
        EstacionManager.cargarDatosReales(
            onSuccess = { apiResponse ->
                mostrarDatosCargados(apiResponse)
            },
            onError = { error ->
                mostrarError(error)
            }
        )
    }
    
    private fun mostrarDatosCargados(apiResponse: ApiResponse) {
        val estadisticas = EstacionManager.obtenerEstadisticas()
        
        val infoText = """
            PRECIO GAS ALERT
            =================
            
            DATOS CARGADOS EXITOSAMENTE
            ===========================
            
            $estadisticas
            
            FUNCIONES DISPONIBLES:
            ✓ Buscar por provincia
            ✓ Buscar por municipio
            ✓ Buscar por marca
            ✓ Encontrar más baratas
            
            TOCA PARA VER ESTADÍSTICAS
        """.trimIndent()
        
        textViewWelcome.text = infoText
        
        // Mostrar toast de confirmación
        Toast.makeText(this, "Datos cargados: ${apiResponse.listaEESSPrecio.size} estaciones", Toast.LENGTH_SHORT).show()
    }
    
    private fun mostrarError(error: String) {
        val infoText = """
            PRECIO GAS ALERT
            =================
            
            ERROR AL CARGAR DATOS
            =====================
            
            $error
            
            POSIBLES CAUSAS:
            • Sin conexión a internet
            • API temporalmente no disponible
            • Error de red
            
            TOCA PARA REINTENTAR
        """.trimIndent()
        
        textViewWelcome.text = infoText
        
        // Mostrar toast de error
        Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
    }
} 