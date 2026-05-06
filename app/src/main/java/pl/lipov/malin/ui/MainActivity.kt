package pl.lipov.malin.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pl.lipov.malin.ui.dashboard.DashboardScreen
import pl.lipov.malin.ui.dashboard.DashboardViewModel
import pl.lipov.malin.ui.theme.MALINTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel() as T
            }
        }
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        Log.d("LifeCycleTraining", "onCreate")
        enableEdgeToEdge()
        setContent {
            MALINTheme {
                Scaffold { padding ->
                    DashboardScreen(
                        modifier = Modifier.padding(padding),
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}