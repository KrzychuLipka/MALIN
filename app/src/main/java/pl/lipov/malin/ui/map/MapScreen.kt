package pl.lipov.malin.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import pl.lipov.malin.common.utils.esri.EsriMapUtils
import pl.lipov.malin.domain.model.Position

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    initialPosition: Position
) {
    val mapUtils = remember { EsriMapUtils() }

    val context = LocalContext.current
    val mapView = remember {
        mapUtils.createMapView(
            context,
            initialPosition.latitude,
            initialPosition.longitude
        )
    }

    LaunchedEffect(initialPosition) {
        val center = mapUtils.getPoint(
            initialPosition.longitude,
            initialPosition.latitude
        )
        mapView.setViewpointCenterAsync(center, EsriMapUtils.INITIAL_SCALE)
    }

    DisposableEffect(Unit) {
        mapView.resume()

        onDispose {
            mapView.pause()
            mapView.dispose()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { mapView }
    )
}