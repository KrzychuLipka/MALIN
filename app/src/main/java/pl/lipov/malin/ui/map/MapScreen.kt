package pl.lipov.malin.ui.map

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.MotionEvent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import kotlinx.coroutines.launch
import pl.lipov.malin.common.utils.esri.EsriMapUtils
import pl.lipov.malin.common.utils.mapBottomSheet.MapFeatureBottomSheet
import pl.lipov.malin.common.utils.mapBottomSheet.MapFeatureInfo
import pl.lipov.malin.domain.model.Position

@Composable
@SuppressLint("ClickableViewAccessibility")
fun MapScreen(
    modifier: Modifier = Modifier,
    initialPosition: Position = Position(
        485735.38681856263,
        637298.6457615903
    ),
    showMarker: Boolean = false
) {
    val context = LocalContext.current
    val mapUtils = remember { EsriMapUtils() }
    var selectedFeature by remember { mutableStateOf<MapFeatureInfo?>(null) }
    val mapView = remember {
        mapUtils.createMapView(
            context,
            initialPosition.latitude,
            initialPosition.longitude
        )
    }
    val graphicsOverlay = remember { GraphicsOverlay() }

    LaunchedEffect(initialPosition) {
        val point2180 = mapUtils.getPoint(
            initialPosition.longitude,
            initialPosition.latitude
        )

        if (showMarker) {
            val symbol = SimpleMarkerSymbol(
                SimpleMarkerSymbol.Style.CIRCLE,
                Color.RED.hashCode(),
                12f
            )
            val graphic = Graphic(point2180, symbol)
            graphicsOverlay.graphics.clear()
            graphicsOverlay.graphics.add(graphic)
        }

        mapView.setViewpointCenterAsync(point2180, EsriMapUtils.INITIAL_SCALE)
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(mapView) {
        mapView.graphicsOverlays.add(graphicsOverlay)
        mapView.onTouchListener = object : DefaultMapViewOnTouchListener(context, mapView) {

            override fun onSingleTapConfirmed(
                event: MotionEvent
            ): Boolean {
                val screenPoint = android.graphics.Point(event.x.toInt(), event.y.toInt())
                coroutineScope.launch {
                    val results = mapView.identifyLayersAsync(
                        screenPoint,
                        EsriMapUtils.IDENTIFY_LAYER_TOLERANCE,
                        false
                    ).get()
                    selectedFeature = mapUtils.extractFeatureInfo(results)
                }
                return super.onSingleTapConfirmed(event)
            }
        }
    }

    MapFeatureBottomSheet(
        feature = selectedFeature,
        onDismiss = { selectedFeature = null },
        map = {
            AndroidView(
                modifier = modifier.fillMaxSize(),
                factory = { mapView },
                onRelease = { mapView.dispose() }
            )
        }
    )
}

