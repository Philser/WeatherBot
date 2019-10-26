package philser.api.weather.model

import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Forecast(forecastedWeatherObject: JSONObject) {

    val forecastedWeather: ForecastedWeather = ForecastedWeather(forecastedWeatherObject)
    val epochDateTime: Int = forecastedWeatherObject["dt"] as Int
    val dateTime: LocalDateTime = LocalDateTime.parse(forecastedWeatherObject["dt_txt"] as String, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    fun getReportString(): String {
        val hour = if(dateTime.hour > 9) "${dateTime.hour}" else "0${dateTime.hour}"
        val minute = if(dateTime.minute > 9) "${dateTime.minute}" else "0${dateTime.minute}"
        return "----Weather at ${hour}:${minute}----\n" +
                forecastedWeather.getWeatherReportString()
    }
}