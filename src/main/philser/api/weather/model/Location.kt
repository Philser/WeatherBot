package philser.api.weather.model

class Location {

    val city: String
    val countryISOCode: String

    companion object {
       val AVAILABLE_LOCATIONS = mapOf(
               "Dresden" to Location("Dresden", "de"),
               "Chemnitz" to Location("Chemnitz", "de")
        )
    }

    private constructor(city: String, countryISOCode: String) {
        this.city = city
        this.countryISOCode = countryISOCode
    }
}