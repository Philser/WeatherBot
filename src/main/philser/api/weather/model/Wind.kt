package philser.api.weather.model

import org.json.JSONObject

class Wind(windObject: JSONObject) {

    // If the wind speed is a whole number, the API omits the decimal point
    val speedMeterPerSecond = if (windObject["speed"].toString().contains('.'))
        windObject["speed"] as Double else (windObject["speed"] as Int).toDouble()
    private val degrees = if (windObject.has("deg")) windObject["deg"] as Int else null
    val direction = getWindDirection(degrees)

    private fun getWindDirection(degrees: Int?): String? {
        degrees ?: return null
        when (degrees) {
            in 0..11 -> return "N"
            in 11..33 -> return "NNE"
            in 33..56 -> return "NE"
            in 56..78 -> return "ENE"
            in 78..101 -> return "E"
            in 101..123 -> return "ESE"
            in 123..146 -> return "SE"
            in 146..168 -> return "SSE"
            in 168..191 -> return "S"
            in 191..213 -> return "SSW"
            in 213..236 -> return "SW"
            in 236..258 -> return "WSW"
            in 258..281 -> return "W"
            in 281..303 -> return "WNW"
            in 303..326 -> return "NW"
            in 326..348 -> return "NNW"
            in 348..360 -> return "N"
            else -> throw Exception("Degrees for wind direction invalid: " + degrees)
        }
    }
}