package philser.api.weather

import philser.api.weather.model.CurrentWeather
import philser.api.weather.model.FiveDaysForecast
import philser.api.weather.model.Location
import philser.logger
import java.util.logging.Level

class WeatherApi(apiToken: String) {

    private val BASE_URL = "http://api.openweathermap.org/data/2.5/"
    private val API_TOKEN = apiToken
    private val logger  by logger()

    fun getCurrentWeather(location: Location): CurrentWeather {

        val url = BASE_URL + "weather?q=${location.city},${location.countryISOCode}&appid=$API_TOKEN&units=metric"
        logger.debug("Sending GET to $url")
        val response = khttp.get(url).jsonObject

        return CurrentWeather(response)
    }

    fun getFiveDaysForecast(location: Location): FiveDaysForecast {
        val url = BASE_URL + "forecast?q=${location.city},${location.countryISOCode}&appid=$API_TOKEN&units=metric"
        logger.debug("Sending GET to $url")
        val response = khttp.get(url).jsonObject

        return FiveDaysForecast(response)
    }

}