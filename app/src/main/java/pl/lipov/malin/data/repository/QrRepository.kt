package pl.lipov.malin.data.repository

import pl.lipov.malin.common.ResultState
import pl.lipov.malin.domain.model.Position
import pl.lipov.malin.domain.repository.QrApi

class QrRepository(
    private val api: QrApi
) {

    companion object {
        private const val QR_KEY = "qr_text"
    }

    suspend fun getPosition(
        qrText: String
    ): ResultState<Position> {
        return try {
            val response = api.getQrData(filter = "$QR_KEY='$qrText'")
            val geometry = response.features.firstOrNull()?.geometry
            if (geometry == null) {
                ResultState.Error(Throwable("Geometry not found."))
            } else {
                ResultState.Success(Position(geometry.x, geometry.y))
            }
        } catch (exception: Exception) {
            ResultState.Error(exception)
        }
    }
}
