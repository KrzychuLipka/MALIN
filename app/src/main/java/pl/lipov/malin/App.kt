package pl.lipov.malin

import android.app.Application
import pl.lipov.malin.common.utils.esri.EsriMapUtils

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        EsriMapUtils().setUpArcGISRuntimeEnvironment(
            license = getString(R.string.arc_gis_license),
            apiKey = getString(R.string.maps_api_key)
        )
    }
}
