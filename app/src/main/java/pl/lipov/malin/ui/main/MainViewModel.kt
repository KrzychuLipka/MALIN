package pl.lipov.malin.ui.main

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

class MainViewModel(
    private val repository: QrRepository
) : ViewModel() {

    companion object {
        private const val TAG = "pw.MainViewModel"
    }

    private val _uiState = MutableStateFlow<ResultState<Pair<Double, Double>>?>(null)
    val uiState: StateFlow<ResultState<Pair<Double, Double>>?> = _uiState

    private var qrScanningInProgress = false

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
