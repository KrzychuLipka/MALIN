package pl.lipov.malin.domain.model

data class ReferenceBeacon(
    val uid: String,
    val name: String,
    val longitude: Double,
    val latitude: Double,
    val floorId: Int
)
