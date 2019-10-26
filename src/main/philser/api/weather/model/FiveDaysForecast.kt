package philser.api.weather.model

import org.json.JSONArray
import org.json.JSONObject
import philser.util.Utils

class FiveDaysForecast(forecastObject: JSONObject) {

    val forecasts: List<Forecast> = createForecastsFromJson(forecastObject)
    val sunrise = Utils.epochToLocalDateTime((((forecastObject["city"] as JSONObject)["sunrise"]) as Int).toLong(), "UTC")
    val sunset = Utils.epochToLocalDateTime((((forecastObject["city"] as JSONObject)["sunset"]) as Int).toLong(), "UTC")
    val cityName = (forecastObject["city"] as JSONObject)["name"]

    private fun createForecastsFromJson(forecastObject: JSONObject): List<Forecast> {
        val forecasts = mutableListOf<Forecast>()
        for (forecast in forecastObject["list"] as JSONArray) {
            forecasts.add(Forecast(forecast as JSONObject))
        }

        return forecasts
    }
}