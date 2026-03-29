package pl.lipov.malin.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import pl.lipov.malin.data.dataSources.BeaconDataSource
import pl.lipov.malin.data.mappers.toDomain
import pl.lipov.malin.domain.model.ReferenceBeacon
import pl.lipov.malin.domain.model.ScannedBeacon
import pl.lipov.malin.domain.useCase.ScanBeaconsUseCase

class MainViewModel(
    private val beaconDataSource: BeaconDataSource,
    private val scanBeaconsUseCase: ScanBeaconsUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "pw.MainViewModel"
    }

    private val _referenceBeacons = MutableStateFlow<List<ReferenceBeacon>>(emptyList())
    val referenceBeacons: StateFlow<List<ReferenceBeacon>> = _referenceBeacons
    private val _scannedBeacons = MutableStateFlow<List<ScannedBeacon>>(emptyList())
    val scannedBeacons: StateFlow<List<ScannedBeacon>> = _scannedBeacons

    fun loadReferenceBeacons() {
        viewModelScope.launch {
            val result = beaconDataSource.loadBeacons()
            result.onSuccess { beaconsDto ->
                _referenceBeacons.value = beaconsDto.map { it.toDomain() }
            }.onFailure { error ->
                Log.e(TAG, "Błąd wczytywania beaconów", error)
            }
        }
    }

    fun startBleScanning() {
        viewModelScope.launch {
            scanBeaconsUseCase()
                .catch { exception ->
                    Log.e(TAG, exception.localizedMessage, exception)
                }
                .collect {
                    _scannedBeacons.value = it
                }
        }
    }
}
