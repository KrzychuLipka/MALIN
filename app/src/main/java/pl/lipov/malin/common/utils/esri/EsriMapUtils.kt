package pl.lipov.malin.common.utils.esri

import android.content.Context
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.view.MapView

class EsriMapUtils {

    companion object {
        const val INITIAL_SCALE = 200.0
        private const val MAX_SCALE = 100.0
        private const val MIN_SCALE = 1000000.0
    }

    val arcGISMap: ArcGISMap by lazy {
        ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC).apply {
            maxScale = MAX_SCALE
            minScale = MIN_SCALE
        }
    }

    fun createMapView(
        context: Context,
        lat: Double,
        lng: Double,
        spatialRef: SpatialRef = SpatialRef.CS92
    ): MapView = MapView(context).apply {
        map = arcGISMap
        val center = getPoint(lng, lat, spatialRef)
        setViewpointCenterAsync(center, INITIAL_SCALE)
    }

    fun getPoint(
        x: Double,
        y: Double,
        spatialRef: SpatialRef = SpatialRef.CS92
    ): Point {
        val source = Point(x, y, SpatialReference.create(spatialRef.wkid))
        return GeometryEngine.project(
            source,
            SpatialReference.create(SpatialRef.WGS_84.wkid)
        ) as Point
    }
}
