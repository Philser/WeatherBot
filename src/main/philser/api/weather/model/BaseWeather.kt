package philser.api.weather.model

import org.json.JSONArray
import org.json.JSONObject

abstract class BaseWeather {

    val weather: WeatherType
    val temperature: Double
    val humidityPercent: Int
    val cloudiness: Int
    val visibilityMeters: Int?
    val wind: Wind

    constructor(weatherObject: JSONObject) {
        weather = WeatherType((weatherObject["weather"] as JSONArray)[0] as JSONObject)
        temperature = (weatherObject["main"] as JSONObject)["temp"] as Double
        humidityPercent = (weatherObject["main"] as JSONObject)["humidity"] as Int
        cloudiness = (weatherObject["clouds"] as JSONObject)["all"] as Int
        visibilityMeters = if (weatherObject.has("visiblity")) weatherObject["visibility"] as Int else null
        wind = Wind(weatherObject["wind"] as JSONObject)
    }

    abstract fun getWeatherReportString(): String
}