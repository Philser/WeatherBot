package philser.api.weather.model

import org.json.JSONObject
import philser.util.Utils
import java.time.LocalDateTime

class Forecast(forecastAPIObject: JSONObject) {

    val weather: Weather = getWeatherFromForecast(forecastAPIObject)

    private fun getWeatherFromForecast(forecastAPIObject: JSONObject): Weather {
        val sunrise = Utils.epochToLocalDateTime((((forecastAPIObject["city"] as JSONObject)["sunrise"]) as Int).toLong(), "UTC")
        val sunset = Utils.epochToLocalDateTime((((forecastAPIObject["city"] as JSONObject)["sunset"]) as Int).toLong(), "UTC")
        val city = (forecastAPIObject["city"] as JSONObject)["name"] as String
        return Weather(forecastAPIObject, city, sunrise, sunset)
    }

    val datetime: Int = forecastAPIObject["dt"] as Int
    val datetimeString: String = forecastAPIObject["dt_text"] as String

    fun getForecastReport(): String {
        return weather.getWeatherReportString()
    }
}