package main.philser.bot

import main.philser.api.telegram.model.KeyboardButton
import main.philser.api.telegram.model.ReplyKeyboardMarkup
import main.philser.bot.db.DBHandler
import philser.api.telegram.BotApi
import philser.api.telegram.model.Chat
import philser.api.telegram.model.Message
import philser.api.telegram.model.Update
import philser.api.telegram.model.User
import philser.api.weather.WeatherApi
import philser.api.weather.model.CurrentWeather
import philser.api.weather.model.Location
import java.time.LocalDateTime
import java.time.ZoneId

class Bot(apiToken: String, weatherApiToken: String, dbHandler: DBHandler) {

    private var api = BotApi(apiToken)
    private var weatherApi = WeatherApi(weatherApiToken)
    private val dbHandler = dbHandler

    // TODO: Add some kind of time limit before next update
    fun runBot() {
        while (true) {
            // Poll for updates
            val updates: List<Update> = pollForUpdates()

            // Process updates
            handleUpdates(updates)

            // Fetch weather data for every location users have subscribed to
            for (location in dbHandler.getSubscribedLocations()) {
                val weather = weatherApi.getCurrentWeather(Location.AVAILABLE_LOCATIONS[location]!!)
                // Output weather data
                for (user in dbHandler.getSubscriptions()) {
                    sendWeatherUpdate(user.value, weather)
                }
            }
        }
    }

    // TODO: Do not hardcode units
    private fun sendWeatherUpdate(chatID: Int, weather: CurrentWeather) {
        val weatherText = "##### Today's weather report #####\n" +
                "-------- Currently:\n" +
                "Weather: ${weather.weather.description}\n" +
                "Temperature: ${weather.temperature} °C\n" +
                "Wind: ${weather.wind.speedMeterPerSecond} m/s in ${weather.wind.direction}\n" +
                "Visibility: ${weather.visibilityMeters}m\n" +
                "\n" +
                "-------- Today's forecast:"
        api.sendMessage(chatID, weatherText)
    }

    private fun pollForUpdates(): List<Update> {
        return api.getUpdates()
    }

    // TODO: Handle other events?
    private fun handleUpdates(updates: List<Update>) {
        val lastProcessedUpdate = dbHandler.getLastReceivedUpdateIDAndTime() ?: Pair<Int, Int>(0, 0) // Null if table is empty, set time to 0
        var updatesToProcess = updates
        val beforeSevenDaysTime = LocalDateTime.now().minusDays(6) // After seven days the update IDs become random again

        if(lastProcessedUpdate.second >= beforeSevenDaysTime.atZone(ZoneId.systemDefault()).toEpochSecond()) // Time has not expired, we still can filter by updateIDs
            updatesToProcess = updatesToProcess.filter { it.updateId > lastProcessedUpdate.first }

        for (update in updatesToProcess) {
            try {
                if (update.message != null)
                    handleMessage(update.message)

                updateProcessedUpdates(update)
            } catch (e: Exception) {
                throw e
                // TODO: Logging
                // TODO: Error handling
            }
        }
    }

    private fun updateProcessedUpdates(update: Update) {
        dbHandler.updateLastReceivedUpdate(update.updateId, System.currentTimeMillis() / 1000L)
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
                if(!dbHandler.getSubscriptions().containsKey(message.user.id)) // Subscribe user if they are new
                    sendMessage(message.chat, subscribeUser(message.user, message.chat))
                sendMessage(message.chat, addUserLocation(message.user, message.text.toString()))
            }
            else -> {
                sendMessage(message.chat, "I do not understand this command.")
            }
        }
    }

    private fun addUserLocation(user: User, locationName: String): String {
        val location = Location.AVAILABLE_LOCATIONS.getValue(locationName)

        dbHandler.addSubscribedLocation(user.id, location.city)
        val subscribedLocations = dbHandler.getSubscribedLocationsForUser(user.id)
        if (subscribedLocations.contains(location.city))
            return "You are already subscribed to weather information for ${location.city}"

        dbHandler.addSubscribedLocation(user.id, location.city)
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
        if (!dbHandler.getUsers().any { it.id == user.id })
            return "You haven't even subscribed yet!"

        val chatId = dbHandler.getSubscriptionByUserID(user.id).second
        dbHandler.deleteAllSubscribedLocationsForUser(user.id)
        dbHandler.deleteSubscription(user.id, chatId)
        dbHandler.deleteChat(chatId)
        dbHandler.deleteUser(user.id)
        return "You have successfully unsubscribed from Dresden's best weather bot. Sad to see you leave!"
    }

    private fun subscribeUser(user: User, chat: Chat): String {
        if (dbHandler.getUsers().any { it.id == user.id })
            return "You are already subscribed, silly!"

        dbHandler.addUser(user)
        dbHandler.addSubscription(user.id, chat.id)
        return "You have successfully subscribed to Dresden's best weather bot. Welcome aboard!"
    }
}