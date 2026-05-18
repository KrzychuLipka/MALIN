package pl.lipov.malin.common.utils.esri

import android.content.Context
import android.util.Log
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.arcgisservices.LabelDefinition
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.BasemapStyle
import com.esri.arcgisruntime.mapping.labeling.SimpleLabelExpression
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.security.AuthenticationChallenge
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.UserCredential
import com.esri.arcgisruntime.symbology.TextSymbol
import com.esri.arcgisruntime.symbology.UniqueValueRenderer
import pl.lipov.malin.common.utils.mapBottomSheet.FeatureCentroid
import pl.lipov.malin.common.utils.mapBottomSheet.MapFeatureInfo
import pl.lipov.malin.domain.model.IndoorContext
import kotlin.math.roundToInt

class EsriMapUtils {

    companion object {
        const val IDENTIFY_LAYER_TOLERANCE = 12.0

        private const val TAG = "pw.EsriMapUtils"
        private const val SERVICE_USER_NAME = "ud_app_conn"
        private const val SERVICE_PASSWORD = "GU$%xPz6r3YyAVB"
        private const val CENAGIS_ARCGIS_SERVER_URL = "https://arcgis.cenagis.edu.pl"
        private const val BUILDINGS_API_URL =
            "${CENAGIS_ARCGIS_SERVER_URL}/server/rest/services/SION2_Topo_MV/sion2_topo_indoor_all/MapServer"
        private const val QR_CODE_TABLE =
            "$CENAGIS_ARCGIS_SERVER_URL/server/rest/services/SION2_Geoopisy/sion_topo_qrcode_edit/MapServer/0"
        private const val LABELING_QUERY =
            "nazwa_skrocona NOT LIKE 'nr_nieznany' OR nazwa_skrocona NOT LIKE 'nieznany'"

        private const val OUTDOOR_INSTALLATIONS_URL = "$BUILDINGS_API_URL/6"
        private const val ROOMS_LAYER_ID = 5
        private const val ROOMS_URL = "$BUILDINGS_API_URL/$ROOMS_LAYER_ID"
        private const val WINDOWS_URL = "$BUILDINGS_API_URL/0"
        private const val STAIRS_INTERNAL_INSTALLATIONS_URL = "$BUILDINGS_API_URL/4"
        private const val DOORS_URL = "$BUILDINGS_API_URL/1"
        private const val WALLS_INTERNAL_URL = "$BUILDINGS_API_URL/3"
        private const val WALLS_EXTERNAL_URL = "$BUILDINGS_API_URL/2"
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
        WALLS_EXTERNAL_URL,
        WALLS_INTERNAL_URL,
        DOORS_URL,
        WINDOWS_URL,
        ROOMS_URL,
        OUTDOOR_INSTALLATIONS_URL,
        STAIRS_INTERNAL_INSTALLATIONS_URL
    )
    private val qrCodeTable by lazy {
        ServiceFeatureTable(QR_CODE_TABLE).apply {
            credential = userCredential
        }
    }

    private fun getFeatureLayers() = featureLayerUrls.map { url ->
        val featureTable = getServiceFeatureTable(url)
        val featureLayer = FeatureLayer(featureTable)
        featureLayer.definitionExpression = getDefinitionExpression(url)
        featureLayer.addDoneLoadingListener {
            if (featureLayer.loadStatus == LoadStatus.LOADED) {
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
                Log.e(TAG, error?.message ?: "Loading feature error: ${error.errorCode}")
            }
        }
        featureLayer
    }

    private fun getDefinitionExpression(
        url: String,
        indoorContext: IndoorContext? = null
    ): String? {
        val buildingId = indoorContext?.buildingId ?: 39
        val floor = indoorContext?.floor ?: 1
        return when (url) {
            WALLS_EXTERNAL_URL,
            WALLS_INTERNAL_URL,
            OUTDOOR_INSTALLATIONS_URL,
            WINDOWS_URL,
            DOORS_URL ->
                "budynek_id = $buildingId"

            ROOMS_URL,
            STAIRS_INTERNAL_INSTALLATIONS_URL ->
                "budynek_id = $buildingId AND poziom = $floor"

            else -> null
        }
    }

    fun setUpArcGISRuntimeEnvironment(
        license: String,
        apiKey: String
    ) {
        ArcGISRuntimeEnvironment.setLicense(license)
        ArcGISRuntimeEnvironment.setApiKey(apiKey)
        val authenticationChallengeHandler = AuthenticationChallengeHandler {
            if (it.type == AuthenticationChallenge.Type.USER_CREDENTIAL_CHALLENGE) {
                AuthenticationChallengeResponse(
                    AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
                    userCredential
                )
            } else {
                AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL, null)
            }
        }
        AuthenticationManager.setAuthenticationChallengeHandler(authenticationChallengeHandler)
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

    fun extractFeatureInfo(
        results: List<IdentifyLayerResult>
    ): MapFeatureInfo? {

        val firstLayerWithElements = results
            .firstOrNull { it.elements.isNotEmpty() }
            ?: return null

        val firstFeature = firstLayerWithElements
            .elements
            .firstOrNull() as? Feature
            ?: return null

        val attrs = firstFeature.attributes

        val title = attrs[RoomFeatureAttr.SHORT_NAME.value]?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: attrs[RoomFeatureAttr.LONG_NAME.value]?.toString()
            ?: "Niezidentyfikowane pomieszczenie"

        val fullName = attrs[RoomFeatureAttr.LONG_NAME.value]?.toString()
        val floor = attrs[RoomFeatureAttr.FLOOR.value]?.toString()

        val subtitle = listOfNotNull(fullName, floor)
            .joinToString(" • ")
            .ifBlank { null }

        val tags = buildList {
            attrs[RoomFeatureAttr.FUNCTION.value]?.toString()?.let {
                add("Funkcja: $it")
            }

            attrs[RoomFeatureAttr.AREA.value]?.toString()?.toDoubleOrNull()?.let {
                add("Powierzchnia: ${it.roundToInt()} m²")
            }

            attrs[RoomFeatureAttr.CAPACITY.value]?.toString()?.let {
                add("Pojemność: $it os.")
            }

            attrs[RoomFeatureAttr.ORGANIZATION.value]?.toString()?.let {
                add(it)
            }

            attrs[RoomFeatureAttr.FACULTY.value]?.toString()?.let {
                add(it)
            }

            attrs[RoomFeatureAttr.DEPARTMENT.value]?.toString()?.let {
                add(it)
            }
        }

        val centroid2180 = firstFeature.geometry.extent.center

        val centroidWgs84 = GeometryEngine.project(
            centroid2180,
            SpatialReference.create(4326) // WGS 84
        ) as Point

        val featureCentroid = FeatureCentroid(
            longitude = centroidWgs84.x,
            latitude = centroidWgs84.y
        )

        return MapFeatureInfo(
            title = title,
            subtitle = subtitle,
            centroid = featureCentroid,
            tags = tags
        )
    }

    private fun applyFilters(
        indoorContext: IndoorContext
    ) {
        arcGISMap.operationalLayers
            .filterIsInstance<FeatureLayer>()
            .forEach { layer ->

                val table = layer.featureTable as? ServiceFeatureTable
                    ?: return@forEach

                val url = table.uri.toString()

                val newExpression =
                    getDefinitionExpression(url, indoorContext)

                if (layer.definitionExpression != newExpression) {
                    layer.definitionExpression = newExpression

                    table.clearCache(false)

                    layer.clearSelection()

                    table.populateFromServiceAsync(
                        QueryParameters().apply {
                            whereClause = "1=1"
                        },
                        true,
                        null
                    )
                }
            }
    }
}
