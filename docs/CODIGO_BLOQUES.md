# LAB 12 — Bloques de código

Dependencias y fragmentos correspondientes a la pantalla del mapa. Las polilíneas son líneas que conectan puntos en el orden indicado; se usan para ilustrar recorridos, no representan rutas calculadas.

## Bloque de código 1 — Dependencias Maps Compose

```kotlin
dependencies {
    val mapsComposeVersion = "4.4.1"

    // Google Maps Compose
    implementation("com.google.maps.android:maps-compose:$mapsComposeVersion")

    // Utilidades de Google Maps para Jetpack Compose
    implementation("com.google.maps.android:maps-compose-utils:$mapsComposeVersion")

    // Widgets de Google Maps Compose
    implementation("com.google.maps.android:maps-compose-widgets:$mapsComposeVersion")
}
```

## Bloque de código 2 — Pantalla y posición inicial

```kotlin
private val arequipaLocation = LatLng(-16.4040102, -71.559611)
private val yuraLocation = LatLng(-16.2520984, -71.6836503)

@Composable
fun MapScreen() {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(arequipaLocation, 12f)
    }
    // El contenido del mapa se dibuja dentro de GoogleMap.
}
```

## Bloque de código 3 — MainActivity

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapScreen()
        }
    }
}
```

## Bloque de código 4 — Icono personalizado del marcador

El recurso vectorial `res/drawable/ic_mountain_marker.xml` se convierte a `Bitmap` y se asigna al marcador.

```kotlin
val mountainMarkerIcon = remember(context) {
    BitmapDescriptorFactory.fromBitmap(
        context.bitmapFromVector(R.drawable.ic_mountain_marker)
    )
}

Marker(
    state = rememberMarkerState(position = arequipaLocation),
    icon = mountainMarkerIcon,
    anchor = Offset(0.5f, 1f),
    title = "Arequipa, Perú",
    snippet = "Marcador personalizado con icono de montaña",
)
```

## Bloque de código 5 — Varios marcadores

```kotlin
private data class PointOfInterest(val name: String, val position: LatLng)

private val pointsOfInterest = listOf(
    PointOfInterest("José Luis Bustamante y Rivero", LatLng(-16.433415, -71.5442652)),
    PointOfInterest("Paucarpata", LatLng(-16.4205151, -71.4945209)),
    PointOfInterest("Zamácola", LatLng(-16.3524187, -71.5675994)),
)

pointsOfInterest.forEach { point ->
    Marker(
        state = rememberMarkerState(position = point.position),
        title = point.name,
        snippet = "Punto de interés en Arequipa",
    )
}
```

## Bloque de código 6 — Animación de cámara a Yura

```kotlin
LaunchedEffect(mapLoaded) {
    if (mapLoaded) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(yuraLocation, 12f),
            durationMs = 3_000,
        )
        delay(1_000)
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(LatLng(-16.34, -71.62), 10.4f),
            durationMs = 2_500,
        )
        delay(1_000)
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(arequipaLocation, 11.7f),
            durationMs = 2_500,
        )
    }
}
```

## Bloque de código 7 — Polígonos

```kotlin
private val plazaDeArmasPolygon = listOf(
    LatLng(-16.398866, -71.536961),
    LatLng(-16.398744, -71.536529),
    LatLng(-16.399178, -71.536289),
    LatLng(-16.399299, -71.536721),
)

Polygon(
    points = plazaDeArmasPolygon,
    strokeColor = Color(0xFFB42318),
    fillColor = Color(0x44F04438),
    strokeWidth = 5f,
)
```

El proyecto contiene también los polígonos de Parque Lambramani y Mall Aventura, con estilos diferenciados.

## Bloque de código 8 — Polilínea a Yura

```kotlin
Polyline(
    points = listOf(
        yuraLocation,
        LatLng(-16.3000, -71.6400),
        LatLng(-16.3500, -71.6020),
        arequipaLocation,
    ),
    color = Color(0xFF0F766E),
    width = 8f,
)
```

## Bloque de código 9 — Polilínea entre puntos del sur

```kotlin
Polyline(
    points = listOf(
        pointsOfInterest[0].position,
        arequipaLocation,
        pointsOfInterest[1].position,
    ),
    color = Color(0xFFE07A1F),
    width = 7f,
)
```

La API `Polyline` de Maps Compose conecta puntos consecutivos en el orden de `points`; consulta la [documentación de polilíneas de Google Maps](https://developers.google.com/maps/documentation/android-sdk/shapes#polylines) y el [ejemplo del SDK para Android](https://developers.google.com/maps/documentation/android-sdk/examples/polyline).


