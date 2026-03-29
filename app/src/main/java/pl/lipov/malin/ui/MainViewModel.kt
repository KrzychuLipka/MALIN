package pl.lipov.malin.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.lipov.malin.data.dataSources.BeaconDataSource
import pl.lipov.malin.data.mappers.toDomain
import pl.lipov.malin.domain.model.Beacon

class MainViewModel(
    private val beaconDataSource: BeaconDataSource
) : ViewModel() {

    companion object {
        private const val TAG = "pw.MainViewModel"
    }

    private val _beacons = MutableStateFlow<List<Beacon>>(emptyList())
    val beacons: StateFlow<List<Beacon>> = _beacons

    fun loadBeacons() {
        viewModelScope.launch {
            val result = beaconDataSource.loadBeacons()
            result.onSuccess { beaconsDto ->
                _beacons.value = beaconsDto.map { it.toDomain() }
            }.onFailure { error ->
                Log.e(TAG, "Błąd wczytywania beaconów", error)
            }
        }
    }
}
