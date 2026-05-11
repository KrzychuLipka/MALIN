package pl.lipov.malin.common.utils.esri

enum class ServiceLayers(
    val layerName: String,
    val layerId: Long,
) {
    GEODESCRIPTION_POINT("geoopis_miejsce", 9),
    GEODESCRIPTION_AREA("geoopis_obszar", 8),
    ROOMS("pomieszczenia", 2),
    STAIRCASE(
        "schody, instalacje wewnętrzne",
        3
    ),
    CLUSTERS_BUILDINGS_FLOORS(
        "klastry_budynki_pietra",
        0
    ),
    BUILDING_ENTRANCE_POI(
        "Wejścia i windy",
        13
    ),
    IMPORTANT_PLACE_POI("Ważne miejsca", 12),
    OTHER_PLACE_POI("Pozostałe miejsca", 11);
}


enum class ServiceLayersAttributes(
    val attrName: String,
) {
    SHORT_NAME("nazwa_skrocona"), LONG_NAME("nazwa_pelna"), CLASS("klasa"), FUNCTION("funkcja")
}

enum class ServiceLayersAttributesType(
    val typeName: String,
) {
    TRANSPORT("transportu"), COMMUNICATION("komunikacji"), TOILET("gospodarki odpadami"), STAIRCASE(
        "schody"
    ),
    STAIR("schodek"), DAIS("podest")
}
