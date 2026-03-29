package pl.lipov.malin.data.mappers

import pl.lipov.malin.data.dto.BeaconDto
import pl.lipov.malin.domain.model.ReferenceBeacon

fun BeaconDto.toDomain() = ReferenceBeacon(
    uid = uid,
    name = name,
    longitude = longitude,
    latitude = latitude,
    floorId = floorId
)
