package philser.api.weather.model

import org.json.JSONObject

class Forecast(forecastAPIObject: JSONObject) {

    val weather: Weather = Weather(forecastAPIObject)
    val datetime: Int = forecastAPIObject["dt"] as Int
    val datetimeString: String = forecastAPIObject["dt_text"] as String
}