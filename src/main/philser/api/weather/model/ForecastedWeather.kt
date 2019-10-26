package philser.api.weather.model

import org.json.JSONObject

class ForecastedWeather(weatherObject: JSONObject): BaseWeather(weatherObject) {
    override fun getWeatherReportString(): String {
        return  "Weather: ${this.weather.description}\n" +
                "Temperature: ${this.temperature} °C\n" +
                "Wind: ${this.wind.speedMeterPerSecond} m/s " +
                (if (this.wind.direction != null ) "in ${this.wind.direction}\n" else "") +
                (if (this.visibilityMeters != null ) "Visibility: ${this.visibilityMeters}m\n" else "") +
                "Humidity: ${this.humidityPercent}%\n" +
                "Cloudiness: ${this.cloudiness}%\n"
    }
}