package api.model

private const val ICON_URL = "https://openweathermap.org/img/wn/"

data class WeatherImage(
    val id: Long,
    val main: String,
    val description: String,
    val icon: String
) {
    fun getIconUrl() = ICON_URL.plus(icon).plus("@2x.png")
}
