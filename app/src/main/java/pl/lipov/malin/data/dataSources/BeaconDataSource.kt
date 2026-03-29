package pl.lipov.malin.data.dataSources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.lipov.malin.data.dto.BeaconDto
import java.io.InputStream

class BeaconDataSource(
    private val inputStreamProvider: suspend () -> InputStream,
    private val beaconParser: BeaconParser = BeaconParser()
) {

    companion object {
        private const val MAIN_BUILDING_PREFIX = "GG#"
    }

    suspend fun loadBeacons(): Result<List<BeaconDto>> = runCatching {
        withContext(Dispatchers.IO) {
            inputStreamProvider().use { stream ->
                val beacons = beaconParser.parseFromStream(stream)
                beacons.filter { it.name.startsWith(MAIN_BUILDING_PREFIX) }
            }
        }
    }
}
