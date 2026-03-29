package pl.lipov.malin.domain.repository

import kotlinx.coroutines.flow.Flow
import pl.lipov.malin.domain.model.ScannedBeacon

interface BeaconRepository {
    fun scanBeacons(): Flow<List<ScannedBeacon>>
}
