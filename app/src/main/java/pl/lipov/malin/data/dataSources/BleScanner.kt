package pl.lipov.malin.data.dataSources

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import pl.lipov.malin.domain.model.ScannedBeacon

class BleScanner(
    private val context: Context
) {

    companion object {
        private const val TAG = "pw.BleScanner"
    }

    fun scan(): Flow<List<ScannedBeacon>> = callbackFlow {

        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val scanner = bluetoothManager.adapter?.bluetoothLeScanner

        if (scanner == null) {
            close(IllegalStateException("BLE not available"))
            return@callbackFlow
        }

        val results = mutableMapOf<String, ScannedBeacon>()

        val callback = object : ScanCallback() {

            override fun onScanResult(
                callbackType: Int,
                result: ScanResult
            ) {
                val device = result.device ?: return

                val beacon = ScannedBeacon(
                    uid = device.address,
                    rssi = result.rssi,
                    timestamp = System.currentTimeMillis()
                )

                results[beacon.uid] = beacon
                trySend(results.values.toList())
            }

            override fun onScanFailed(
                errorCode: Int
            ) {
                close(RuntimeException("Scan failed: $errorCode"))
            }
        }

        try {
            scanner.startScan(callback)
        } catch (exception: SecurityException) {
            close(exception)
        }

        awaitClose {
            try {
                scanner.stopScan(callback)
            } catch (exception: SecurityException) {
                Log.e(TAG, exception.localizedMessage, exception)
            }
        }
    }
}