package philser.bot

import philser.api.telegram.BotApi
import philser.api.telegram.model.Chat
import philser.api.telegram.model.Message
import philser.api.telegram.model.Update
import philser.api.telegram.model.User
import philser.api.weather.WeatherApi
import philser.api.weather.model.CurrentWeather

class Bot(apiToken: String, weatherApiToken: String) {

    var api = BotApi(apiToken)
    var weatherApi = WeatherApi(weatherApiToken)
    var lastProcessedUpdate: Int = 0
    val subscribedUsers: HashMap<User, Chat> = HashMap()

    // TODO: Add some kind of time limit before next update
    fun runBot() {
        while (true) {
            // Poll for updates
            val updates: List<Update> = pollForUpdates()

            // Process updates
            handleUpdates(updates)

            // Fetch weather data
            val weather = weatherApi.getCurrentWeather()

            // Output weather data
            for (user in subscribedUsers) {
                sendWeatherUpdate(user.value, weather)
            }

        }
    }

    private fun sendWeatherUpdate(chat: Chat, weather: CurrentWeather) {
        api.sendMessage(chat.id, "It's currently ${weather.temperature} Kelvin in Dresden")
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
        if (!subscribedUsers.containsKey(user))
            return "You haven't even subscribed yet!"

        subscribedUsers.remove(user)
        return "You have successfully unsubscribed from Dresden's best weather bot. Sad to see you leave!"
    }

    private fun subscribeUser(user: User, chat: Chat): String {
        if (subscribedUsers.containsKey(user))
            return "You are already subscribed, silly!"

        subscribedUsers[user] = chat
        return "You have successfully subscribed to Dresden's best weather bot. Welcome aboard!"
    }
}