package philser.api.weather.model

import org.json.JSONArray
import org.json.JSONObject
import philser.util.Utils
import java.time.LocalDateTime
import java.util.*

class Weather {

    val weather: WeatherType
    val temperature: Double
    val humidityPercent: Int
    val cloudiness: Int
    val visibilityMeters: Int?
    val wind: Wind
    val cityName: String
    val sunriseTime: LocalDateTime
    val sunsetTime: LocalDateTime

    constructor(weatherObject: JSONObject) {

        weather = WeatherType((weatherObject["weather"] as JSONArray)[0] as JSONObject)
        temperature = (weatherObject["main"] as JSONObject)["temp"] as Double
        humidityPercent = (weatherObject["main"] as JSONObject)["humidity"] as Int
        cloudiness = (weatherObject["clouds"] as JSONObject)["all"] as Int
        visibilityMeters = if (weatherObject.has("visiblity")) weatherObject["visibility"] as Int else null
        wind = Wind(weatherObject["wind"] as JSONObject)
        cityName = weatherObject["name"] as String
        sunriseTime = Utils.epochToLocalDateTime(((weatherObject["sys"] as JSONObject)["sunrise"] as Int).toLong(), "UTC")
        sunsetTime = Utils.epochToLocalDateTime(((weatherObject["sys"] as JSONObject)["sunset"] as Int).toLong(), "UTC")
    }

    constructor(forecastWeatherObject: JSONObject, city: String, sunriseTime: LocalDateTime, sunsetTime: LocalDateTime) {
        weather = WeatherType((forecastWeatherObject["weather"] as JSONArray)[0] as JSONObject)
        temperature = (forecastWeatherObject["main"] as JSONObject)["temp"] as Double
        humidityPercent = (forecastWeatherObject["main"] as JSONObject)["humidity"] as Int
        cloudiness = (forecastWeatherObject["clouds"] as JSONObject)["all"] as Int
        visibilityMeters = if (forecastWeatherObject.has("visiblity")) forecastWeatherObject["visibility"] as Int else null
        wind = Wind(forecastWeatherObject["wind"] as JSONObject)
        cityName = city
        this.sunriseTime = sunriseTime
        this.sunsetTime = sunsetTime
    }

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