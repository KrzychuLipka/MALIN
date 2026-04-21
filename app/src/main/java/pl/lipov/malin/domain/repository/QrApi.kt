package pl.lipov.malin.domain.repository

import pl.lipov.malin.domain.dto.QrResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface QrApi {

    @GET("server/rest/services/SION2_Geoopisy/sion_topo_qrcode/MapServer/0/query")
    suspend fun getQrData(
        @Query("where") filter: String,
        @Query("f") format: String = "json"
    ): QrResponseDto
}
