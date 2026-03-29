package pl.lipov.malin.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pl.lipov.malin.data.dataSources.BleScanner
import pl.lipov.malin.domain.model.ScannedBeacon
import pl.lipov.malin.domain.repository.BeaconRepository

class BeaconRepositoryImpl(
    private val scanner: BleScanner
) : BeaconRepository {

    override fun scanBeacons(): Flow<List<ScannedBeacon>> {
        return scanner.scan()
            .map { beacons ->
                beacons
                    .filter { it.rssi > -90 }
                    .sortedByDescending { it.rssi }
                    .take(5)
            }
    }
}
