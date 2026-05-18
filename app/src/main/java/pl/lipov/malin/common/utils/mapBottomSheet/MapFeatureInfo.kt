package pl.lipov.malin.common.utils.mapBottomSheet

data class MapFeatureInfo(
    val title: String,
    val subtitle: String? = null,
    val centroid: FeatureCentroid? = null,
    val tags: List<String> = emptyList(),
)
