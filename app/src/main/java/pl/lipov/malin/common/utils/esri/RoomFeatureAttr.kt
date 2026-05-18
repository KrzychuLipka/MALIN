package pl.lipov.malin.common.utils.esri

enum class RoomFeatureAttr(
    val value: String
) {
    SHORT_NAME("nazwa_skrocona"),
    LONG_NAME("nazwa_pelna"),
    FLOOR("pietro_full"),
    FUNCTION("funkcja"),
    AREA("powierzchnia"),
    CAPACITY("pojemnosc"),
    ORGANIZATION("organizacja"),
    FACULTY("wydzial"),
    DEPARTMENT("departament")
}
