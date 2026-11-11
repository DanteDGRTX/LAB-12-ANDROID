# LAB 12 — Bloques de código

Los ejemplos siguientes corresponden a la app de Google Maps con Jetpack Compose. La polilínea es ilustrativa: une los puntos dados en orden y no calcula una ruta vial.

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

## Bloque de código 2 — Crear el mapa Compose

```kotlin
package com.example.lab12_maps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private val arequipaLocation = LatLng(-16.4040102, -71.559611)

@Composable
fun MapScreen() {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(arequipaLocation, 12f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
        ) {
            Marker(
                state = rememberMarkerState(position = arequipaLocation),
                title = "Arequipa, Perú",
            )
        }
    }
}
```

## Bloque de código 3 — MainActivity

```kotlin
package com.example.lab12_maps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.lab12_maps.ui.MapScreen

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

El recurso vectorial está en `res/drawable/ic_mountain_marker.xml`; se convierte a un bitmap y se usa en el marcador de Arequipa.

```kotlin
var mountainMarkerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

GoogleMap(
    cameraPositionState = cameraPositionState,
    onMapLoaded = { mapLoaded = true },
) {
    mountainMarkerIcon?.let { icon ->
        Marker(
            state = rememberMarkerState(position = arequipaLocation),
            icon = icon,
            anchor = Offset(0.5f, 1f),
            title = "Arequipa, Perú",
        )
    }
}

LaunchedEffect(mapLoaded) {
    if (mapLoaded) {
        mountainMarkerIcon = BitmapDescriptorFactory.fromBitmap(
            context.bitmapFromVector(R.drawable.ic_mountain_marker)
        )
    }
}
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

## Bloque de código 6 — Control de cámara hacia Yura

La cámara espera a que cargue el mapa, va a Yura, muestra el recorrido y termina en una vista cercana de las áreas del centro de Arequipa.

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
        delay(1_000)
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(LatLng(-16.416, -71.522), 14f),
            durationMs = 2_500,
        )
    }
}
```

## Bloque de código 7 — Polígonos de Arequipa

```kotlin
private val mallAventuraPolygon = listOf(
    LatLng(-16.432292, -71.509145),
    LatLng(-16.432757, -71.509626),
    LatLng(-16.433013, -71.509310),
    LatLng(-16.432566, -71.508853),
)

private val parqueLambramaniPolygon = listOf(
    LatLng(-16.422704, -71.530830),
    LatLng(-16.422920, -71.531340),
    LatLng(-16.423264, -71.531110),
    LatLng(-16.423050, -71.530600),
)

private val plazaDeArmasPolygon = listOf(
    LatLng(-16.398866, -71.536961),
    LatLng(-16.398744, -71.536529),
    LatLng(-16.399178, -71.536289),
    LatLng(-16.399299, -71.536721),
)

Polygon(points = plazaDeArmasPolygon, strokeColor = Color(0xFFB42318), fillColor = Color(0x88F04438), strokeWidth = 12f)
Polygon(points = parqueLambramaniPolygon, strokeColor = Color(0xFF175CD3), fillColor = Color(0x883B82F6), strokeWidth = 12f)
Polygon(points = mallAventuraPolygon, strokeColor = Color(0xFF6941C6), fillColor = Color(0x889B8AFB), strokeWidth = 12f)
```

## Bloque de código 8 — Polilínea de Yura a Arequipa

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

## Bloque de código 9 — Polilínea entre puntos de interés

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

## Bloque de código 10 — Convertir vector drawable a bitmap

```kotlin
private fun Context.bitmapFromVector(@DrawableRes drawableId: Int): Bitmap {
    val drawable = requireNotNull(ContextCompat.getDrawable(this, drawableId))
    val width = drawable.intrinsicWidth.coerceAtLeast(1)
    val height = drawable.intrinsicHeight.coerceAtLeast(1)
    return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
    }
}
```

Maps Compose provides composables for polylines and polygons. See the [Google Maps Compose project documentation](https://github.com/googlemaps/android-maps-compose) and Google's [polylines and shapes documentation](https://developers.google.com/maps/documentation/android-sdk/shapes#polylines).


## Ejercicio de desarrollo 1 — Cambiar el tipo de mapa

Maps Compose permite cambiar el mapa en ejecución mediante `MapProperties(mapType = ...)`. El siguiente ejemplo conserva el tipo seleccionado en estado Compose y ofrece Normal, Satélite, Terreno e Híbrido:

```kotlin
private data class MapTypeOption(val label: String, val type: MapType)

private val mapTypeOptions = listOf(
    MapTypeOption("Normal", MapType.NORMAL),
    MapTypeOption("Satélite", MapType.SATELLITE),
    MapTypeOption("Terreno", MapType.TERRAIN),
    MapTypeOption("Híbrido", MapType.HYBRID),
)

@Composable
fun MapScreen() {
    var selectedMapType by remember { mutableStateOf(MapType.NORMAL) }
    var mapTypeMenuExpanded by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = MapProperties(mapType = selectedMapType),
        )

        Box(Modifier.align(Alignment.TopEnd).padding(16.dp)) {
            Button(onClick = { mapTypeMenuExpanded = true }) {
                Text("Tipo de mapa: ${mapTypeOptions.first { it.type == selectedMapType }.label}")
            }
            DropdownMenu(
                expanded = mapTypeMenuExpanded,
                onDismissRequest = { mapTypeMenuExpanded = false },
            ) {
                mapTypeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            selectedMapType = option.type
                            mapTypeMenuExpanded = false
                        },
                    )
                }
            }
        }
    }
}
```

La documentación del [Maps SDK para Android](https://developers.google.com/maps/documentation/android-sdk/configure-map) describe los tipos de mapa y sus diferencias. En este proyecto se actualizan mediante las propiedades de `GoogleMap` de [Maps Compose](https://github.com/googlemaps/android-maps-compose).
