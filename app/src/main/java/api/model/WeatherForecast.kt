package api.model

import api.model.DailyForecast

data class WeatherForecast(
    val daily: List<DailyForecast>
)
