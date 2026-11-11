package com.example.lab12_maps.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.lab12_maps.R
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

private val arequipaLocation = LatLng(-16.4040102, -71.559611)
private val yuraLocation = LatLng(-16.2520984, -71.6836503)

private data class PointOfInterest(val name: String, val position: LatLng)

private val pointsOfInterest = listOf(
    PointOfInterest("José Luis Bustamante y Rivero", LatLng(-16.433415, -71.5442652)),
    PointOfInterest("Paucarpata", LatLng(-16.4205151, -71.4945209)),
    PointOfInterest("Zamácola", LatLng(-16.3524187, -71.5675994)),
)

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

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(arequipaLocation, 12f)
    }
    var mapLoaded by remember { mutableStateOf(false) }
    var mountainMarkerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var yuraMarkerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    // Maps Compose requires the map to be loaded before camera operations and map SDK icons.
    LaunchedEffect(mapLoaded) {
        if (mapLoaded) {
            mountainMarkerIcon = BitmapDescriptorFactory.fromBitmap(
                context.bitmapFromVector(R.drawable.ic_mountain_marker)
            )
            yuraMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            cameraPositionState.animate(
                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(yuraLocation, 11.3f),
                durationMs = 3_000,
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { mapLoaded = true },
        ) {
            mountainMarkerIcon?.let { icon ->
                Marker(
                    state = rememberMarkerState(position = arequipaLocation),
                    icon = icon,
                    anchor = Offset(0.5f, 1f),
                    title = "Arequipa, Perú",
                    snippet = "Marcador personalizado con icono de montaña",
                )
            }

            pointsOfInterest.forEach { point ->
                Marker(
                    state = rememberMarkerState(position = point.position),
                    title = point.name,
                    snippet = "Punto de interés en Arequipa",
                )
            }

            yuraMarkerIcon?.let { icon ->
                Marker(
                    state = rememberMarkerState(position = yuraLocation),
                    icon = icon,
                    title = "Yura",
                    snippet = "Destino de la animación de cámara",
                )
            }

            Polygon(
                points = plazaDeArmasPolygon,
                strokeColor = Color(0xFFB42318),
                fillColor = Color(0x44F04438),
                strokeWidth = 5f,
            )
            Polygon(
                points = parqueLambramaniPolygon,
                strokeColor = Color(0xFF175CD3),
                fillColor = Color(0x443B82F6),
                strokeWidth = 5f,
            )
            Polygon(
                points = mallAventuraPolygon,
                strokeColor = Color(0xFF6941C6),
                fillColor = Color(0x449B8AFB),
                strokeWidth = 5f,
            )

            // Example route connecting Yura to central Arequipa.
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

            // Example route linking the southern points of interest.
            Polyline(
                points = listOf(
                    pointsOfInterest[0].position,
                    arequipaLocation,
                    pointsOfInterest[1].position,
                ),
                color = Color(0xFFE07A1F),
                width = 7f,
            )
        }
    }
}

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

