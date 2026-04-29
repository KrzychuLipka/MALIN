package pl.lipov.malin.ui.dashboard

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.lipov.malin.common.ResultState
import pl.lipov.malin.common.utils.QrCodeScannerUtils
import pl.lipov.malin.data.repository.QrRepository
import pl.lipov.malin.domain.model.Position
import pl.lipov.malin.domain.repository.QrApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DashboardViewModel : ViewModel() {

    companion object {
        private const val TAG = "pw.MainViewModel"
    }

    private fun provideApi(): QrApi {
        return Retrofit.Builder()
            .baseUrl("https://arcgis.cenagis.edu.pl/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QrApi::class.java)
    }

    val api = provideApi()
    val repository = QrRepository(api)

    private val historicalPositions = mutableListOf<Position>()
    private val _uiState = MutableStateFlow<ResultState<Position>?>(null)
    val uiState: StateFlow<ResultState<Position>?> = _uiState

    private var qrScanningInProgress = false

    fun addHistoricalPosition(
        position: Position
    ) {
        historicalPositions.add(position)
    }

    fun launchGmsQrCodeScanner(
        activity: Activity,
        errorCallback: () -> Unit,
    ) {
        if (qrScanningInProgress) return
        qrScanningInProgress = true
        QrCodeScannerUtils.setUpGmsBarcodeScanningApi(activity)
        QrCodeScannerUtils.checkGmsBarcodeScanningModuleAvailability(
            successCallback = {
                if (it.areModulesAvailable()) {
                    launchScanner(activity)
                } else {
                    installModule(activity, errorCallback)
                }
            },
            errorCallback = {
                Log.e(TAG, it.localizedMessage, it)
                errorCallback()
                cancelQrScanning()
            }
        )
    }

    fun startScanViaZxingScanner(
        launcher: ActivityResultLauncher<ScanOptions>,
    ) {
        QrCodeScannerUtils.startScanViaZxingScanner(
            launcher
        ) {
            Log.e(TAG, it.localizedMessage, it)
            cancelQrScanning()
        }
    }

    private fun installModule(
        context: Context,
        errorCallback: () -> Unit,
    ) {
        QrCodeScannerUtils.installGmsBarcodeScanningModule(
            successCallback = { launchScanner(context) },
            errorCallback = {
                Log.e(TAG, it.localizedMessage, it)
                errorCallback()
            }
        )
    }

    private fun launchScanner(
        context: Context
    ) {
        QrCodeScannerUtils.startScanViaGmsBarcodeScanner(context)
            .addOnSuccessListener { handleQrContent(it.rawValue) }
            .addOnCanceledListener { cancelQrScanning() }
            .addOnFailureListener {
                Log.e(TAG, it.localizedMessage, it)
                cancelQrScanning()
            }
    }

    fun handleQrContent(
        qrContent: String?
    ) {
        if (qrContent.isNullOrBlank()) {
            Log.e(TAG, "Empty QR")
            cancelQrScanning()
            return
        }
        val qrText = qrContent.substringAfterLast("/")
        fetchPosition(qrText)
    }

    private fun fetchPosition(
        qrText: String
    ) {
        viewModelScope.launch {
            _uiState.value = ResultState.Loading
            when (val result = repository.getPosition(qrText)) {
                is ResultState.Success -> {
                    _uiState.value = result
                }

                is ResultState.Error -> {
                    _uiState.value = result
                    Log.e(TAG, result.throwable.message, result.throwable)
                }

                else -> Unit
            }
            cancelQrScanning()
        }
    }

    fun cancelQrScanning() {
        qrScanningInProgress = false
    }
}
