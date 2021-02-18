package api.model

import com.google.gson.annotations.SerializedName

data class CurrentWeatherForecast(
        val id: Long,
        @SerializedName("main")
    val weather: CurrentWeather,
        @SerializedName("weather")
    val weatherImage: List<WeatherImage>
) : RetroDate
