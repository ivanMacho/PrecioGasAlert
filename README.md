# Precio Gas Alert

**Precio Gas Alert** es una aplicación Android que permite consultar, filtrar y monitorizar en tiempo real los precios de combustibles en las estaciones de servicio de España, utilizando la ubicación del usuario y filtros avanzados para mostrar solo las opciones más relevantes y económicas.

---

## Funcionalidad principal

- **Consulta de precios de combustible**: Muestra una lista de estaciones de servicio con los precios actualizados de diferentes tipos de combustible.
- **Filtrado avanzado**: Permite filtrar por tipo de combustible, precio máximo, distancia máxima y tipo de venta (pública/reservada).
- **Ordenación**: Las estaciones pueden ordenarse por distancia o por precio.
- **Monitorización en segundo plano**: Un servicio de fondo monitoriza la ubicación y notifica si hay nuevas estaciones que cumplen los filtros.
- **Persistencia**: Los datos y filtros se guardan localmente, permitiendo que la app funcione incluso sin conexión y conserve las preferencias del usuario.
- **Interfaz retro**: Inspirada en terminales antiguos, con fondo negro, tipografía monospace y colores verde/cian/amarillo.

---

## Estructura lógica y técnica

### 1. **Modelo de datos**

- `EstacionTerrestre`: Representa una estación de servicio, con todos los campos relevantes del API oficial (nombre, dirección, coordenadas, precios de todos los combustibles, etc).
- `ApiResponse`: Modelo de la respuesta completa del API, incluyendo la lista de estaciones, fecha y estado de la consulta.
- `EstacionFilter`: Modelo de los filtros seleccionados por el usuario (tipo de combustible, precio máximo, distancia máxima, orden, tipo de venta).

### 2. **Gestión de datos y lógica de negocio**

- **`EstacionManager`**: Objeto singleton que centraliza la lógica de:
  - Carga y persistencia de datos (usando `SharedPreferences`).
  - Aplicación de filtros y ordenación.
  - Obtención de estadísticas y utilidades.
  - Llamadas al API real y parseo de la respuesta.
- **`ApiService`**: Encapsula la comunicación HTTP con el API oficial, parseando la respuesta JSON a los modelos de datos.
- **`JsonLoader`**: Utilidad para cargar y parsear un archivo JSON de ejemplo (usado solo para desarrollo y definición de modelos).

### 3. **Interfaz de usuario**

- **Pantalla principal (`MainActivity`)**:
  - Muestra el título, resumen de filtros activos, lista de estaciones filtradas, fecha de los datos y dos FABs (refrescar y ajustes/filtros).
  - Solicita permisos de localización y obtiene la ubicación del usuario para filtrar por distancia.
  - Permite refrescar manualmente los datos y muestra animaciones de carga.
- **Pantalla de filtros (`FilterActivity`)**:
  - Permite seleccionar tipo de combustible, precio máximo y distancia máxima mediante menús y sliders.
  - Guarda los filtros en persistencia local.
- **Pantalla de ajustes (`SettingsActivity`)**:
  - Permite elegir el criterio de ordenación y el tipo de venta.
  - Permite ajustar la frecuencia y distancia mínima de comprobación del servicio de monitorización.
- **Servicio de monitorización (`LocationMonitorService`)**:
  - Corre en segundo plano, monitoriza la ubicación y lanza notificaciones si hay nuevas estaciones que cumplen los filtros.
- **Splash (`SplashActivity`)**:
  - Pantalla de bienvenida con animación de fade-in.

### 4. **Persistencia y preferencias**

- Se utiliza `SharedPreferences` para guardar:
  - Últimos datos descargados.
  - Filtros y preferencias del usuario.
  - Parámetros del servicio de monitorización.

### 5. **Permisos y seguridad**

- Solicita y comprueba permisos de localización en tiempo de ejecución.
- Maneja correctamente la ausencia de permisos, mostrando mensajes claros al usuario.

### 6. **Componentes visuales**

- **Layouts XML**: Definidos en `res/layout`, con uso intensivo de Material Components y personalización retro.
- **Colores y temas**: Definidos en `res/values/colors.xml` y `themes.xml`, con fondo negro, verde, cian y amarillo.
- **Tipografía**: Monospace en todos los textos principales para dar el look retro.

---

## Explicación de cada parte

### MainActivity

- Controla el ciclo de vida principal.
- Inicializa el gestor de estaciones y la UI.
- Solicita permisos de localización y obtiene la ubicación.
- Muestra la lista de estaciones filtradas y actualiza la UI según los datos y filtros.
- Permite refrescar datos y acceder a la pantalla de filtros.

### EstacionManager

- Centraliza la lógica de negocio.
- Carga y guarda datos y filtros.
- Aplica los filtros y ordenación seleccionados.
- Llama al API y parsea la respuesta.
- Proporciona utilidades para obtener estadísticas y datos filtrados.

### ApiService

- Realiza la petición HTTP al API oficial.
- Parsea la respuesta JSON a los modelos de datos.
- Maneja errores de red y parseo.

### JsonLoader

- Carga y parsea un archivo JSON de ejemplo para desarrollo.
- Permite definir y probar la estructura de los modelos de datos.

### FilterActivity

- Permite al usuario seleccionar filtros avanzados.
- Guarda los filtros en persistencia local.
- Utiliza menús desplegables y sliders para una experiencia intuitiva.

### SettingsActivity

- Permite ajustar preferencias avanzadas (orden, tipo de venta, frecuencia y distancia de monitorización).
- Guarda las preferencias y reinicia el servicio de monitorización si es necesario.

### LocationMonitorService

- Servicio en segundo plano que monitoriza la ubicación.
- Lanza notificaciones si hay nuevas estaciones que cumplen los filtros.
- Configurable en frecuencia y distancia mínima.

### EstacionAdapter

- Adaptador para el RecyclerView de estaciones.
- Muestra nombre, precio y distancia de cada estación.
- Permite abrir la ubicación en Google Maps al pulsar sobre una estación.

### SplashActivity

- Muestra una pantalla de bienvenida con animación antes de entrar a la app.

---

## Instalación y ejecución

1. **Clona el repositorio**:
   ```bash
   git clone https://github.com/tuusuario/preciogasalert.git
   ```
2. **Abre el proyecto en Android Studio**.
3. **Configura una API Key de Google Maps** (opcional, solo si quieres mapas embebidos).
4. **Ejecuta la app en un emulador o dispositivo físico** (Android 6.0+ recomendado).
5. **Concede permisos de localización** cuando la app lo solicite.

---

## Permisos requeridos

- **Localización precisa**: Para filtrar estaciones por distancia y monitorizar en segundo plano.
- **Acceso a Internet**: Para descargar los datos del API oficial.

---

## Créditos

- Datos oficiales: [Ministerio para la Transición Ecológica y el Reto Demográfico, Gobierno de España](https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/)
- Iconos: Material Icons