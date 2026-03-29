package pl.lipov.malin.domain.model

data class ScannedBeacon(
    val uid: String,
    val rssi: Int,
    val timestamp: Long
)
