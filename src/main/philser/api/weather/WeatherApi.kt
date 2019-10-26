package philser.api.weather

import philser.api.weather.model.Weather
import philser.api.weather.model.Location

class WeatherApi(apiToken: String) {

    private val BASE_URL = "http://api.openweathermap.org/data/2.5/"
    private val API_TOKEN = apiToken

    fun getCurrentWeather(location: Location): Weather {
        val url = BASE_URL + "weather?q=${location.city},${location.countryISOCode}&appid=$API_TOKEN&units=metric"
        val response = khttp.get(url).jsonObject

        return Weather(response)
    }

}