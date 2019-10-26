package philser.api.weather.model

import org.json.JSONArray
import org.json.JSONObject
import philser.util.Utils
import java.time.LocalDateTime

class Weather(weatherObject: JSONObject) {

    val weather: WeatherType = WeatherType((weatherObject["weather"] as JSONArray)[0] as JSONObject)
    val temperature: Double = (weatherObject["main"] as JSONObject)["temp"] as Double
    val humidityPercent: Int = (weatherObject["main"] as JSONObject)["humidity"] as Int
    val cloudiness: Int = (weatherObject["clouds"] as JSONObject)["all"] as Int
    val visibilityMeters: Int? = if (weatherObject.has("visiblity")) weatherObject["visibility"] as Int else null
    val wind: Wind = Wind(weatherObject["wind"] as JSONObject)
    val cityName: String = weatherObject["name"] as String
    val sunriseTime: LocalDateTime = Utils.epochToLocalDateTime(((weatherObject["sys"] as JSONObject)["sunrise"] as Int).toLong(), "UTC")
    val sunsetTime: LocalDateTime = Utils.epochToLocalDateTime(((weatherObject["sys"] as JSONObject)["sunset"] as Int).toLong(), "UTC")

    fun getWeatherReportString(): String {
        return  "Weather: ${this.weather.description}\n" +
                "Temperature: ${this.temperature} °C\n" +
                "Wind: ${this.wind.speedMeterPerSecond} m/s " +
                (if (this.wind.direction != null ) "in ${this.wind.direction}\n" else "") +
                (if (this.visibilityMeters != null ) "Visibility: ${this.visibilityMeters}m\n" else "") +
                "Humidity: ${this.humidityPercent}%\n" +
                "Cloudiness: ${this.cloudiness}%\n" +
                "Sunrise: ${this.sunriseTime.hour}:${this.sunriseTime.minute}\n" +
                "Sunset: ${this.sunsetTime.hour}:${this.sunsetTime.minute}"
    }

}