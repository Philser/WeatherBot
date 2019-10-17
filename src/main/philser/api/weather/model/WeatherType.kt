package philser.api.weather.model

import org.json.JSONObject

class WeatherType(weatherObject: JSONObject) {
    val id: Int = weatherObject["id"] as Int
    val description: String = weatherObject["description"] as String
}