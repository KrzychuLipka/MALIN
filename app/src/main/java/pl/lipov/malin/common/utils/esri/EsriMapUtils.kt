package pl.lipov.malin.common.utils.esri

import android.content.Context
import android.util.Log
import com.esri.arcgisruntime.arcgisservices.LabelDefinition
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.labeling.SimpleLabelExpression
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.security.UserCredential
import com.esri.arcgisruntime.symbology.TextSymbol
import com.esri.arcgisruntime.symbology.UniqueValueRenderer

class EsriMapUtils {

    companion object {
        private const val TAG = "pw.EsriMapUtils"
        private const val SERVICE_USER_NAME = "ud_app_conn"
        private const val SERVICE_PASSWORD = "GU$%xPz6r3YyAVB"
        private const val CENAGIS_ARCGIS_SERVER_URL = "https://arcgis.cenagis.edu.pl"
        private const val CLUSTERS_API_URL =
            "${CENAGIS_ARCGIS_SERVER_URL}/server/rest/services/SION2_Topo_MV/sion2_wfs_klastry_budynki/MapServer/1"
        private const val BUILDINGS_API_URL =
            "${CENAGIS_ARCGIS_SERVER_URL}/server/rest/services/SION2_Topo_MV/sion2_topo_indoor_all/MapServer"
        private const val LABELING_QUERY =
            "nazwa_skrocona NOT LIKE 'nr_nieznany' OR nazwa_skrocona NOT LIKE 'nieznany'"

        private const val OUTDOOR_INSTALLATIONS_URL = "$BUILDINGS_API_URL/6"
        private const val ROOMS_URL = "$BUILDINGS_API_URL/5"
        const val INITIAL_SCALE = 1500.0
        private const val MAX_SCALE = 100.0
        private const val MIN_SCALE = 1000000.0
    }

    val arcGISMap: ArcGISMap by lazy {
        ArcGISMap(BasemapStyle.ARCGIS_TOPOGRAPHIC).apply {
            maxScale = MAX_SCALE
            minScale = MIN_SCALE
            addDoneLoadingListener {
                val loadError = arcGISMap.loadError
                if (loadError != null) {
                    Log.e(TAG, "Map loading error: ${loadError.cause?.localizedMessage}")
                    return@addDoneLoadingListener
                }
                if (arcGISMap.loadStatus == LoadStatus.LOADED) {
                    Log.d(TAG, "Map loaded")
                    showFeatureLayers()
                }
            }
        }
    }

    private val userCredential = UserCredential(SERVICE_USER_NAME, SERVICE_PASSWORD)

    private val featureLayerUrls: List<String> = listOf(
        CLUSTERS_API_URL,
        OUTDOOR_INSTALLATIONS_URL,
        ROOMS_URL
    )

    private fun getFeatureLayers() = featureLayerUrls.map { url ->

        val featureTable = getServiceFeatureTable(url)

        val featureLayer = FeatureLayer(featureTable)

        featureLayer.addDoneLoadingListener {

            if (featureLayer.loadStatus == LoadStatus.LOADED) {

                Log.d(
                    TAG,
                    "feature table name: ${featureLayer.featureTable.tableName}"
                )

                if (featureLayer.featureTable.tableName == ServiceLayers.ROOMS.layerName) {
                    featureLayer.isLabelsEnabled = true
                    setLabelDefinition(featureLayer)
                    updateRenderer(featureLayer, CorruptedClassAttrs.EDUCATION)
                    updateRenderer(featureLayer, CorruptedClassAttrs.INDUSTRIAL)
                }

                setCorruptedLayerSymbology(
                    featureLayer,
                    CorruptedClassAttrs.BUILDING_ENTRANCE_POI.corruptedName
                )

                setCorruptedLayerSymbology(
                    featureLayer,
                    CorruptedClassAttrs.IMPORTANT_PLACE_POI.corruptedName
                )

                setCorruptedLayerSymbology(
                    featureLayer,
                    CorruptedClassAttrs.OTHER_PLACE_POI.corruptedName
                )

            } else {

                val error = featureLayer.loadError

                Log.e(
                    TAG,
                    $$"""
                Feature layer failed:
                code=$${error?.errorCode}
                message=$${error?.message}
                cause=${error?.cause}
                cause=$${error?.cause}
                additional=$${error?.additionalMessage}
                url=$$url
                """.trimIndent()
                )
            }
        }

        featureLayer
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

    private fun getServiceFeatureTable(
        url: String,
    ): ServiceFeatureTable = ServiceFeatureTable(url)
        .apply { credential = userCredential }

    private fun ArcGISMap.showFeatureLayers() {
        val featureLayers = getFeatureLayers()
        operationalLayers?.let {
            if (operationalLayers.isNotEmpty()) {
                operationalLayers.clear()
            }
            operationalLayers.addAll(featureLayers)
        }
    }

    private fun setLabelDefinition(featureLayer: FeatureLayer) {
        val labelExpression = SimpleLabelExpression().apply {
            expression = "[${ServiceLayersAttributes.SHORT_NAME.attrName}]"
        }
        val textSymbol = TextSymbol().apply {
            text = ""
            color = android.graphics.Color.BLACK
            size = 12f
            horizontalAlignment = TextSymbol.HorizontalAlignment.CENTER
            verticalAlignment = TextSymbol.VerticalAlignment.MIDDLE
        }
        val labelDefinition = LabelDefinition(labelExpression, textSymbol)
        labelDefinition.whereClause = LABELING_QUERY
        if (featureLayer.labelDefinitions.isEmpty()) {
            featureLayer.labelDefinitions.add(labelDefinition)
        }
    }


    private fun updateRenderer(featureLayer: FeatureLayer, attributeName: CorruptedClassAttrs) {
        val renderer = featureLayer.renderer as UniqueValueRenderer
        renderer.uniqueValues.forEach {
            if (it.label == attributeName.corruptedName) {
                val properName = attributeName.properName
                val valueList: MutableList<Any> = ArrayList()
                valueList.add(properName)
                val newUniqueValue =
                    UniqueValueRenderer.UniqueValue(properName, properName, it.symbol, valueList)
                renderer.uniqueValues.add(newUniqueValue)
            }
        }
    }

    private fun setCorruptedLayerSymbology(featureLayer: FeatureLayer, corruptedLayerName: String) {
        if (featureLayer.featureTable.tableName == corruptedLayerName) {
            featureLayer.isLabelsEnabled = true
            setLabelDefinition(featureLayer)
        }
    }
}
