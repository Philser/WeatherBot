package philser.api.weather.model

import org.json.JSONArray
import org.json.JSONObject
import philser.api.weather.WeatherApi

class CurrentWeather(weatherObject: JSONObject) {

    val weather: WeatherType = WeatherType((weatherObject["weather"] as JSONArray)[0] as JSONObject)
    val temperature: Double = (weatherObject["main"] as JSONObject)["temp"] as Double
    val humidityPercent: Int = (weatherObject["main"] as JSONObject)["humidity"] as Int
    val cloudiness: Int = (weatherObject["clouds"] as JSONObject)["all"] as Int
    val visibilityMeters: Int? = if (weatherObject.has("visiblity")) weatherObject["visibility"] as Int else null
    val wind: Wind = Wind(weatherObject["wind"] as JSONObject)

    fun getWeatherReportString(): String {
        return  "Weather: ${this.weather.description}\n" +
                "Temperature: ${this.temperature} °C\n" +
                "Wind: ${this.wind.speedMeterPerSecond} m/s " +
                if (this.wind.direction != null ) "in ${this.wind.direction}\n" else "" +
                if (this.visibilityMeters != null ) "Visibility: ${this.visibilityMeters}m\n" else "" +
                "Humidity: ${this.humidityPercent}%\n" +
                "Cloudiness: ${this.cloudiness}%"
    }

}