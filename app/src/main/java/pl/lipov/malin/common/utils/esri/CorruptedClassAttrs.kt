package pl.lipov.malin.common.utils.esri

enum class CorruptedClassAttrs(
    val corruptedName: String,
    val properName: String
) {
    EDUCATION("dzia?alno?ci edukacyjnej lub badawczej", "działalności edukacyjnej lub badawczej"),
    INDUSTRIAL("przemys?owo-techniczny", "przemysłowy"),
    BUILDING_ENTRANCE_POI("Wej?cia do budynk�w", "Wejścia do budynków"),
    IMPORTANT_PLACE_POI("Wa?ne miejsca", "Ważne miejsca"),
    OTHER_PLACE_POI("Pozosta?e miejsca", "Pozostałe miejsca")
}
