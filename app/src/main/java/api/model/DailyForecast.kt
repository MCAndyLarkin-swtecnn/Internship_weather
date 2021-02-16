package api.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class DailyForecast(
        val dt: Long,
        val temp: Temperature,
        @SerializedName("weather")
    val weatherImage: List<WeatherImage>
) {
    fun getDate(): String = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).run {
            format(Date(dt * 1000L).apply { timeZone = TimeZone.getTimeZone("Europe/Moscow") })
    }
}
