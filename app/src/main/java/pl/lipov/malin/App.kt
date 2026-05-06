package pl.lipov.malin

import android.app.Application
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.security.AuthenticationChallenge
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse
import com.esri.arcgisruntime.security.AuthenticationManager
import com.esri.arcgisruntime.security.UserCredential

class App : Application() {

    companion object {
        private const val SERVICE_USER_NAME = "ud_app_conn"
        private const val SERVICE_PASSWORD = "StaryJezNiesieJ4pk@"
    }

    private val userCredential = UserCredential(SERVICE_USER_NAME, SERVICE_PASSWORD)

    override fun onCreate() {
        super.onCreate()
        setUpArcGISRuntimeEnvironment()
    }

    fun setUpArcGISRuntimeEnvironment() {
        ArcGISRuntimeEnvironment.setLicense(getString(R.string.arc_gis_license))
        ArcGISRuntimeEnvironment.setApiKey(getString(R.string.maps_api_key))
        val authenticationChallengeHandler = AuthenticationChallengeHandler {
            if (it.type == AuthenticationChallenge.Type.USER_CREDENTIAL_CHALLENGE) {
                AuthenticationChallengeResponse(
                    AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL,
                    userCredential
                )
            } else {
                AuthenticationChallengeResponse(AuthenticationChallengeResponse.Action.CANCEL, null)
            }
        }
        AuthenticationManager.setAuthenticationChallengeHandler(authenticationChallengeHandler)
    }
}