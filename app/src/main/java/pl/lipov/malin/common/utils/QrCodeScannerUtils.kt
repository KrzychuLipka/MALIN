package pl.lipov.malin.common.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.common.moduleinstall.ModuleAvailabilityResponse
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallClient
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.journeyapps.barcodescanner.ScanOptions

object QrCodeScannerUtils {
    private const val TAG = "pw.GmsQrCodeScannerUtils"

    private var gmsBarcodeScanningApi: GmsBarcodeScanner? = null
    private var moduleInstallClient: ModuleInstallClient? = null

    fun setUpGmsBarcodeScanningApi(
        activity: Activity
    ) {
        moduleInstallClient = ModuleInstall.getClient(activity)
        gmsBarcodeScanningApi = GmsBarcodeScanning.getClient(activity)
    }

    fun checkGmsBarcodeScanningModuleAvailability(
        successCallback: (ModuleAvailabilityResponse) -> Unit,
        errorCallback: (Exception) -> Unit,
    ) {
        Log.d(TAG, "Checking GmsBarcodeScanner availability...")
        moduleInstallClient
            ?.areModulesAvailable(gmsBarcodeScanningApi)
            ?.addOnSuccessListener {
                Log.d(TAG, "areModulesAvailable: ${it.areModulesAvailable()}")
                successCallback(it)
            }
            ?.addOnFailureListener {
                Log.e(TAG, it.localizedMessage, it)
                errorCallback(it)
            }
    }

    fun installGmsBarcodeScanningModule(
        successCallback: () -> Unit,
        errorCallback: (Exception) -> Unit,
    ) {
        Log.d(TAG, "Installing GmsBarcodeScanner...")
        val api = gmsBarcodeScanningApi ?: return
        val moduleInstallRequest = ModuleInstallRequest
            .newBuilder()
            .addApi(api)
            .setListener { moduleInstallStatusUpdate ->
                when (moduleInstallStatusUpdate.installState) {
                    ModuleInstallStatusUpdate.InstallState.STATE_FAILED -> {
                        errorCallback(RuntimeException("Install state: FAILED"))
                    }

                    ModuleInstallStatusUpdate.InstallState.STATE_COMPLETED -> successCallback()
                    else -> Log.d(
                        TAG,
                        "GmsBarcodeScanner installState: ${moduleInstallStatusUpdate.installState}"
                    )
                }
            }
            .build()
        moduleInstallClient
            ?.installModules(moduleInstallRequest)
            ?.addOnFailureListener { exception ->
                errorCallback(exception)
            }
    }

    fun startScanViaGmsBarcodeScanner(
        context: Context
    ): Task<Barcode> {
        Log.d(TAG, "Start scanning...")
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .allowManualInput()
            .enableAutoZoom()
            .build()
        val scanner = GmsBarcodeScanning.getClient(context, options)
        return scanner.startScan()
    }

    fun startScanViaZxingScanner(
        activityResultLauncher: ActivityResultLauncher<ScanOptions>,
        errorCallback: (Exception) -> Unit
    ) {
        try {
            val scanOption = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setCameraId(0)
            }
            activityResultLauncher.launch(scanOption)
        } catch (exception: Exception) {
            errorCallback(exception)
        }
    }
}
