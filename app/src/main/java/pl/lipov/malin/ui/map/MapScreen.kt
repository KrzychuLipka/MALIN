package pl.lipov.malin.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

private const val BASE_ZOOM_LEVEL = 18.0
private const val MAP_STYLE = "https://basemaps.cartocdn.com/gl/dark-matter-gl-style/style.json"

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    initialLocation: LatLng,
    mapStyle: String = MAP_STYLE,
    baseZoomLevel: Double = BASE_ZOOM_LEVEL
) {
    val context = LocalContext.current
    remember { MapLibre.getInstance(context) }
    val mapView = rememberMapView()
    AndroidView(
        modifier = modifier,
        factory = {
            mapView.setUp(initialLocation, mapStyle, baseZoomLevel)
        }
    )
}

@Composable
fun rememberMapView(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply { onCreate(null) }
    }
    MapDisposableEffect(LocalLifecycleOwner.current, mapView)
    return mapView
}

@Composable
private fun MapDisposableEffect(
    lifecycleOwner: LifecycleOwner,
    mapView: MapView
) = DisposableEffect(lifecycleOwner, mapView) {
    val observer = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            mapView.onStart()
        }

        override fun onResume(owner: LifecycleOwner) {
            mapView.onResume()
        }

        override fun onPause(owner: LifecycleOwner) {
            mapView.onPause()
        }

        override fun onStop(owner: LifecycleOwner) {
            mapView.onStop()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            mapView.onDestroy()
        }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}

private fun MapView.setUp(
    initialLocation: LatLng,
    mapStyle: String = MAP_STYLE,
    baseZoomLevel: Double = BASE_ZOOM_LEVEL
): MapView {
    getMapAsync { map ->
        map.cameraPosition =
            CameraPosition
                .Builder()
                .target(initialLocation)
                .zoom(baseZoomLevel)
                .build()
        map.setStyle(mapStyle)
        map.addMarker(initialLocation)
    }
    return this
}

private fun MapLibreMap.addMarker(
    position: LatLng
) {
    val markerOptions = MarkerOptions()
        .apply {
            setPosition(position)
        }
    addMarker(markerOptions)
}
