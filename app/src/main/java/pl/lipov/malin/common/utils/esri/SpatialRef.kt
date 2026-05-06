package pl.lipov.malin.common.utils.esri

enum class SpatialRef(
    val wkid: Int
) {
    CS92(2180),
    WGS_84(3857)
}
