package com.example.lab12_maps.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import com.example.lab12_maps.R
import com.google.android.gms.location.CancellationTokenSource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.delay
import java.util.Locale

private val arequipaLocation = LatLng(-16.4040102, -71.559611)
private val yuraLocation = LatLng(-16.2520984, -71.6836503)

private data class PointOfInterest(val name: String, val position: LatLng)

private data class MapTypeOption(val label: String, val type: MapType)

private val mapTypeOptions = listOf(
    MapTypeOption("Normal", MapType.NORMAL),
    MapTypeOption("Satélite", MapType.SATELLITE),
    MapTypeOption("Terreno", MapType.TERRAIN),
    MapTypeOption("Híbrido", MapType.HYBRID),
)

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
    var selectedMapType by remember { mutableStateOf(MapType.NORMAL) }
    var mapTypeMenuExpanded by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var locationDetails by remember { mutableStateOf<String?>(null) }
    var isFindingLocation by remember { mutableStateOf(false) }
    val fusedLocationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun requestCurrentLocation() {
        isFindingLocation = true
        fusedLocationClient.requestSingleCurrentLocation(
            onResult = { location ->
                isFindingLocation = false
                if (location == null) {
                    Toast.makeText(context, "No se obtuvo una ubicación. Activa la ubicación del emulador e inténtalo de nuevo.", Toast.LENGTH_LONG).show()
                } else {
                    userLocation = LatLng(location.latitude, location.longitude)
                    locationDetails = String.format(
                        Locale.getDefault(),
                        "Lat %.5f, Lon %.5f (±%.0f m)",
                        location.latitude,
                        location.longitude,
                        location.accuracy,
                    )
                }
            },
            onFailure = { error ->
                isFindingLocation = false
                Toast.makeText(context, "Error de ubicación: " + (error.localizedMessage ?: "inténtalo de nuevo"), Toast.LENGTH_LONG).show()
            },
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) requestCurrentLocation()
        else Toast.makeText(context, "Concede el permiso de ubicación para usar esta función.", Toast.LENGTH_LONG).show()
    }

    val locateUser = {
        if (context.hasLocationPermission()) {
            hasLocationPermission = true
            requestCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            )
        }
    }

    LaunchedEffect(userLocation) {
        userLocation?.let { location ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(location, 16f),
                durationMs = 1_200,
            )
        }
    }
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
                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(yuraLocation, 12f),
                durationMs = 3_000,
            )
            delay(1_000)
            cameraPositionState.animate(
                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                    LatLng(-16.34, -71.62),
                    10.4f,
                ),
                durationMs = 2_500,
            )
            delay(1_000)
            cameraPositionState.animate(
                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                    arequipaLocation,
                    11.7f,
                ),
                durationMs = 2_500,
            )
            delay(1_000)
            cameraPositionState.animate(
                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                    LatLng(-16.416, -71.522),
                    14f,
                ),
                durationMs = 2_500,
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = selectedMapType,
                isMyLocationEnabled = hasLocationPermission,
            ),
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
                fillColor = Color(0x88F04438),
                strokeWidth = 12f,
            )
            Polygon(
                points = parqueLambramaniPolygon,
                strokeColor = Color(0xFF175CD3),
                fillColor = Color(0x883B82F6),
                strokeWidth = 12f,
            )
            Polygon(
                points = mallAventuraPolygon,
                strokeColor = Color(0xFF6941C6),
                fillColor = Color(0x889B8AFB),
                strokeWidth = 12f,
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

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(onClick = locateUser, enabled = !isFindingLocation) {
                Text(if (isFindingLocation) "Buscando ubicación…" else "Mi ubicación")
            }
            locationDetails?.let { details ->
                Surface(shape = RoundedCornerShape(8.dp), tonalElevation = 3.dp) {
                    Text(
                        text = details,
                        modifier = Modifier.padding(10.dp).widthIn(max = 260.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Box {
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
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
private fun FusedLocationProviderClient.requestSingleCurrentLocation(
    onResult: (Location?) -> Unit,
    onFailure: (Exception) -> Unit,
) {
    val cancellation = CancellationTokenSource()
    getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellation.token)
        .addOnSuccessListener(onResult)
        .addOnFailureListener(onFailure)
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





