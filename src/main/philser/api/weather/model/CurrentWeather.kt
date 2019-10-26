package philser.api.weather.model

import org.json.JSONArray
import org.json.JSONObject
import philser.util.Utils
import java.time.LocalDateTime
import java.util.*

class CurrentWeather: BaseWeather{


    val cityName: String
    val sunriseTime: LocalDateTime
    val sunsetTime: LocalDateTime

    constructor(weatherObject: JSONObject) : super(weatherObject) {
        cityName = weatherObject["name"] as String
        sunriseTime = Utils.epochToLocalDateTime(((weatherObject["sys"] as JSONObject)["sunrise"] as Int).toLong(), "UTC")
        sunsetTime = Utils.epochToLocalDateTime(((weatherObject["sys"] as JSONObject)["sunset"] as Int).toLong(), "UTC")
    }

    override fun getWeatherReportString(): String {
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