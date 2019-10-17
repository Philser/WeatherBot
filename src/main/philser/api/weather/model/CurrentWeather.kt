package philser.api.weather.model

import org.json.JSONArray
import org.json.JSONObject

class CurrentWeather(weatherObject: JSONObject) {

    val weather: WeatherType = WeatherType((weatherObject["weather"] as JSONArray)[0] as JSONObject)
    val temperature: Double = (weatherObject["main"] as JSONObject)["temp"] as Double
    val humidityPercent: Int = (weatherObject["main"] as JSONObject)["humidity"] as Int
    val cloudiness: Int = (weatherObject["clouds"] as JSONObject)["all"] as Int
    val visibilityMeters: Int = weatherObject["visibility"] as Int
    val wind: Wind = Wind(weatherObject["wind"] as JSONObject)
}