package philser.api.weather

import philser.api.weather.model.CurrentWeather

class WeatherApi(apiToken: String) {

    private val BASE_URL = "http://api.openweathermap.org/data/2.5/"
    private val API_TOKEN = apiToken

    // TODO: Allow arbitrary (existing) locations
    fun getCurrentWeather(): CurrentWeather {
        val url = BASE_URL + "weather?q=Dresden,de&appid=$API_TOKEN"
        val response = khttp.get(url).jsonObject

        return CurrentWeather(response)
    }

}