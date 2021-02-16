package api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import api.model.CurrentWeatherForecast
import api.model.WeatherForecast

private const val API_KEY = "92dd28f1dc9baf629b40f09a07ff87ac"

interface RetrofitService {

    @GET("data/2.5/onecall")
    fun getWeatherForecast(
        @Query("lat") latitude: Double = 56.302947,
        @Query("lon") longitude: Double = 44.021527,
        @Query("exclude") exclude: String = arrayOf(
            "current",
            "minutely",
            "hourly",
            "alerts"
        ).joinToString(separator = ","),
        @Query("units") units: String = "metric",
        @Query("appId") apiKey: String = API_KEY
    ): Call<WeatherForecast>

    @GET("data/2.5/weather")
    fun getCurrentWeatherForecast(
            @Query("q") place: String = arrayOf(
            "Nizhniy Novgorod", "RUS"
        ).joinToString(separator = ","),
            @Query("appId") apiKey: String = API_KEY,
            @Query("units") units: String = "metric"
    ): Call<CurrentWeatherForecast>

    @GET
    fun getWeatherImage(@Url imageUrl: String): Call<ResponseBody>
}