package pl.lipov.malin.ui.dashboard

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import org.maplibre.android.geometry.LatLng
import pl.lipov.malin.common.ResultState
import pl.lipov.malin.common.utils.cs92ToWgs84
import pl.lipov.malin.domain.model.Position
import pl.lipov.malin.ui.map.MapScreen

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel
) {
    val context = LocalContext.current
    val activity = context as Activity
    val state by viewModel.uiState.collectAsState()
    val launcher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        val content = result.contents
        if (content.isNullOrBlank()) {
            viewModel.cancelQrScanning()
        } else {
            viewModel.handleQrContent(content)
        }
    }
    Column(modifier = modifier.fillMaxSize()) {
        Button(
            onClick = {
                viewModel.launchGmsQrCodeScanner(
                    activity,
                    errorCallback = {
                        viewModel.startScanViaZxingScanner(launcher)
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Skanuj QR")
        }
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is ResultState.Loading -> {
                    Text("Pozycjonowanie...")
                }

                is ResultState.Success -> {
                    val position: Position = (state as ResultState.Success)
                        .data
                        .cs92ToWgs84()
                    MapScreen(
                        initialLocation = LatLng(position.latitude, position.longitude)
                    )
                }

                is ResultState.Error -> {
                    Text("Błąd: ${(state as ResultState.Error).throwable.message}")
                }

                null -> {
                    Text("Brak danych")
                }
            }
        }
    }
}
