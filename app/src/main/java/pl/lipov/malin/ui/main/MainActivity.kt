package pl.lipov.malin.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import pl.lipov.malin.data.dataSources.BeaconDataSource
import pl.lipov.malin.data.dataSources.BleScanner
import pl.lipov.malin.data.repository.BeaconRepositoryImpl
import pl.lipov.malin.domain.model.ReferenceBeacon
import pl.lipov.malin.domain.model.ScannedBeacon
import pl.lipov.malin.domain.repository.BeaconRepository
import pl.lipov.malin.domain.useCase.ScanBeaconsUseCase
import pl.lipov.malin.ui.theme.MALINTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val BEACONS_FILE_NAME = "beacons.json"
    }

    private val dataSource = BeaconDataSource(
        inputStreamProvider = { assets.open(BEACONS_FILE_NAME) }
    )
    private val beaconRepository: BeaconRepository = BeaconRepositoryImpl(
        BleScanner(context = this)
    )
    private val viewModel = MainViewModel(
        beaconDataSource = dataSource,
        scanBeaconsUseCase = ScanBeaconsUseCase(beaconRepository)
    )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MALINTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BeaconList(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        viewModel.loadReferenceBeacons()
        viewModel.startBleScanning()
    }
}

@Composable
fun BeaconList(
    viewModel: MainViewModel,
    modifier: Modifier
) {
    RequestScanBeaconsPermissions()

    val scannedBeacons by viewModel.scannedBeacons.collectAsState()
    val referenceBeacons by viewModel.referenceBeacons.collectAsState()

    Row {
        LazyColumn(modifier.weight(0.5f)) {
            items(referenceBeacons) { beacon: ReferenceBeacon ->
                Text(
                    text = beacon.name,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        LazyColumn(modifier.weight(0.5f)) {
            items(scannedBeacons) { beacon: ScannedBeacon ->
                Text(
                    text = beacon.uid,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
