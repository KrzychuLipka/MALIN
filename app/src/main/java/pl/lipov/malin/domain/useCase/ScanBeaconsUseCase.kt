package pl.lipov.malin.domain.useCase

import kotlinx.coroutines.flow.Flow
import pl.lipov.malin.domain.model.ScannedBeacon
import pl.lipov.malin.domain.repository.BeaconRepository

class ScanBeaconsUseCase(
    private val repository: BeaconRepository
) {
    operator fun invoke(): Flow<List<ScannedBeacon>> = repository.scanBeacons()
}
