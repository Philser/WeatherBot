package philser.bot

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
    private val subscribedUserLocations: HashMap<Int, Location> = HashMap()

    // TODO: Add some kind of time limit before next update
    fun runBot() {
        while (true) {
            // Poll for updates
            val updates: List<Update> = pollForUpdates()

            // Process updates
            handleUpdates(updates)

            // Fetch weather data for every location users have subscribed to
            for (location in subscribedUserLocations.values) {
                val weather = weatherApi.getCurrentWeather(location)
                // Output weather data
                for (user in subscribedUserIDs) {
                    sendWeatherUpdate(user.value, weather)
                }
            }
            break
        }
    }

    private fun sendWeatherUpdate(chat: Chat, weather: CurrentWeather) {
        val weatherText = "##### Today's weather report #####\n" +
                "-------- Currently:\n" +
                "Weather: ${weather.weather.description}\n" +
                "Temperature: ${weather.temperature}\n" +
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
            }
            "/stop" -> {
                sendMessage(message.chat, unsubscribeUser(message.user))
            }
            else -> {
                sendMessage(message.chat, "I do not understand this command.")
            }
        }
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