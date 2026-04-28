package pl.lipov.malin.common.utils

import android.util.Log
import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate
import pl.lipov.malin.domain.model.Position

private const val CS_92 = "EPSG:2180"
private const val CS_92_PROJ_PARAMS =
    "+proj=tmerc +lat_0=0 +lon_0=19 +k=0.9993 +x_0=500000 +y_0=-5300000 +ellps=GRS80 +units=m +no_defs"

private const val WGS_84 = "EPSG:4326"
private const val WGS_84_PROJ_PARAMS = "+proj=longlat +datum=WGS84 +no_defs"


fun Position.transform(
    sourceEPSG: String = CS_92,
    sourceProjParams: String = CS_92_PROJ_PARAMS,
    targetEPSG: String = WGS_84,
    targetProjParams: String = WGS_84_PROJ_PARAMS
): Position {
    val dst = ProjCoordinate()
    val src = ProjCoordinate(longitude, latitude)
    val crsFactory = CRSFactory()
    val sourceCRS = crsFactory.createFromParameters(
        sourceEPSG,
        sourceProjParams
    )
    val targetCRS = crsFactory.createFromParameters(
        targetEPSG,
        targetProjParams
    )
    val transformFactory = CoordinateTransformFactory()
        .createTransform(sourceCRS, targetCRS)
    transformFactory.transform(src, dst)
    Log.d("TRANSFORM", "SRC: x=$longitude y=$latitude")
    Log.d("TRANSFORM", "DST: x=${dst.x} y=${dst.y}")
    return Position(
        latitude = dst.y,
        longitude = dst.x
    )
}
