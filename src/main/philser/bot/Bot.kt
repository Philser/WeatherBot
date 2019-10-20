package philser.bot

import main.philser.api.telegram.model.KeyboardButton
import main.philser.api.telegram.model.ReplyKeyboardMarkup
import philser.api.telegram.BotApi
import philser.api.telegram.model.Chat
import philser.api.telegram.model.Message
import philser.api.telegram.model.Update
import philser.api.telegram.model.User
import philser.api.weather.WeatherApi
import philser.api.weather.model.CurrentWeather
import philser.api.weather.model.Location

class Bot(apiToken: String, weatherApiToken: String) {

    private var api = BotApi(apiToken)
    private var weatherApi = WeatherApi(weatherApiToken)
    private var lastProcessedUpdate: Int = 0
    private val subscribedUserIDs: HashMap<Int, Chat> = HashMap()
    private val subscribedUserLocations: HashMap<Int, MutableList<Location>> = HashMap()
    private val relevantLocations: MutableSet<Location> = mutableSetOf()

    // TODO: Add some kind of time limit before next update
    fun runBot() {
        while (true) {
            // Poll for updates
            val updates: List<Update> = pollForUpdates()

            // Process updates
            handleUpdates(updates)

            // Fetch weather data for every location users have subscribed to
            for (location in relevantLocations) {
                val weather = weatherApi.getCurrentWeather(location)
                // Output weather data
                for (user in subscribedUserIDs) {
                    sendWeatherUpdate(user.value, weather)
                }
            }
        }
    }

    // TODO: Do not hardcode units
    private fun sendWeatherUpdate(chat: Chat, weather: CurrentWeather) {
        val weatherText = "##### Today's weather report #####\n" +
                "-------- Currently:\n" +
                "Weather: ${weather.weather.description}\n" +
                "Temperature: ${weather.temperature} °C\n" +
                "Wind: ${weather.wind.speedMeterPerSecond} m/s in ${weather.wind.direction}\n" +
                "Visibility: ${weather.visibilityMeters}m\n" +
                "\n" +
                "-------- Today's forecast:"
        api.sendMessage(chat.id, weatherText)
    }

    private fun pollForUpdates(): List<Update> {
        return api.getUpdates()
    }

    // TODO: Handle other events?
    private fun handleUpdates(updates: List<Update>) {

        for (update in updates.filter { it.updateId > lastProcessedUpdate }) {
            try {
                if (update.message != null)
                    handleMessage(update.message)

                updateProcessedUpdates(update)
            } catch (e: Exception) {
                // TODO: Logging
                // TODO: Error handling
            }
        }
    }

    private fun updateProcessedUpdates(update: Update) {
        // TODO: Persistence
        lastProcessedUpdate = update.updateId
    }

    private fun handleMessage(message: Message) {
        when(message.text) {
            "/start" -> {
                sendMessage(message.chat, subscribeUser(message.user, message.chat))
                startLocationChoiceDialog(message)
            }
            "/stop" -> {
                sendMessage(message.chat, unsubscribeUser(message.user))
            }
            in Location.AVAILABLE_LOCATIONS.keys -> {
                if(!subscribedUserIDs.containsKey(message.user.id))
                    sendMessage(message.chat, subscribeUser(message.user, message.chat)) // Subscribe user if they are new
                sendMessage(message.chat, addUserLocation(message.user, message.text.toString()))
            }
            else -> {
                sendMessage(message.chat, "I do not understand this command.")
            }
        }
    }

    private fun addUserLocation(user: User, locationName: String): String {
        if (!subscribedUserLocations.containsKey(user.id))
            subscribedUserLocations[user.id] = mutableListOf()

        val location = Location.AVAILABLE_LOCATIONS.getValue(locationName)
        relevantLocations.add(location) // Add location to set of locations to get weather data for

        if (subscribedUserLocations[user.id]!!.contains(location))
            return "You are already subscribed to weather information for $location"

        subscribedUserLocations[user.id]!!.add(location)
        return "You will now receive weather information for $location"
    }

    private fun startLocationChoiceDialog(message: Message) {
        val buttons: MutableList<Array<KeyboardButton>> = mutableListOf()
        for (availableLocation in Location.AVAILABLE_LOCATIONS) {
            var buttonRow = Array(1) { KeyboardButton(availableLocation.key) }
            buttons.add(buttonRow)
        }

        sendMessage(message.chat, "Please choose a location", ReplyKeyboardMarkup(buttons.toTypedArray()))
    }

    private fun sendMessage(chat: Chat, message: String, replyMarkup: ReplyKeyboardMarkup): Message {
        return api.sendMessage(chat.id, message, replyMarkup)
    }

    private fun sendMessage(chat: Chat, message: String): Message {
        return api.sendMessage(chat.id, message)
    }

    private fun unsubscribeUser(user: User): String {
        if (!subscribedUserIDs.containsKey(user.id))
            return "You haven't even subscribed yet!"

        subscribedUserIDs.remove(user.id)
        return "You have successfully unsubscribed from Dresden's best weather bot. Sad to see you leave!"
    }

    private fun subscribeUser(user: User, chat: Chat): String {
        if (subscribedUserIDs.containsKey(user.id))
            return "You are already subscribed, silly!"

        subscribedUserIDs[user.id] = chat
        return "You have successfully subscribed to Dresden's best weather bot. Welcome aboard!"
    }
}