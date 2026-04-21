package pl.lipov.malin.common.utils

import pl.lipov.malin.domain.model.Position
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

private object CS92 {
    const val A = 6378137.0
    const val E2 = 0.00669438002290
    const val SCALE = 0.9993

    const val FALSE_EASTING = 500000.0
    const val FALSE_NORTHING = -5300000.0
}

fun Position.cs92ToWgs84(): Position {

    val x = this.longitude
    val y = this.latitude

    val xAdj = x - CS92.FALSE_EASTING
    val yAdj = y - CS92.FALSE_NORTHING

    val m = yAdj / CS92.SCALE
    val mu = m / (CS92.A * (1 - CS92.E2 / 4 - 3 * CS92.E2.pow(2) / 64 - 5 * CS92.E2.pow(3) / 256))

    val e1 = (1 - sqrt(1 - CS92.E2)) / (1 + sqrt(1 - CS92.E2))

    val fp = mu +
            (3 * e1 / 2 - 27 * e1.pow(3) / 32) * sin(2 * mu) +
            (21 * e1.pow(2) / 16 - 55 * e1.pow(4) / 32) * sin(4 * mu) +
            (151 * e1.pow(3) / 96) * sin(6 * mu) +
            (1097 * e1.pow(4) / 512) * sin(8 * mu)

    val sinFp = sin(fp)
    val cosFp = cos(fp)
    val tanFp = tan(fp)

    val c1 = CS92.E2 * cosFp.pow(2) / (1 - CS92.E2)
    val t1 = tanFp.pow(2)
    val r1 = CS92.A * (1 - CS92.E2) / (1 - CS92.E2 * sinFp.pow(2)).pow(1.5)
    val n1 = CS92.A / sqrt(1 - CS92.E2 * sinFp.pow(2))

    val d = xAdj / (n1 * CS92.SCALE)

    val lat = fp - (n1 * tanFp / r1) *
            (d.pow(2) / 2 -
                    (5 + 3 * t1 + 10 * c1 - 4 * c1.pow(2) - 9 * CS92.E2) * d.pow(4) / 24)

    val lon = toRadians(19.0) +
            (d -
                    (1 + 2 * t1 + c1) * d.pow(3) / 6) / cosFp

    return Position(
        latitude = Math.toDegrees(lat),
        longitude = Math.toDegrees(lon)
    )
}