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
import philser.api.weather.model.Forecast
import philser.api.weather.model.CurrentWeather
import philser.api.weather.model.FiveDaysForecast
import philser.api.weather.model.Location
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class Bot(apiToken: String, weatherApiToken: String, private val dbHandler: DBHandler) {

    private var api = BotApi(apiToken)
    private var weatherApi = WeatherApi(weatherApiToken)

    fun runBot() {
        while (true) {
            // Poll for updates
            val updates: List<Update> = pollForUpdates()

            // Process updates
            handleUpdates(updates)

            // Fetch weather data for every location users have subscribed to
            trySendDailyWeatherUpdate()
            trySendDailyForecast()
        }
    }

    /**
     * Send daily forecast if it is time to do so
     */
    private fun trySendDailyForecast() {
        val updateTime = LocalTime.of(7, 0)
        if (LocalTime.now() == updateTime) {
            for (location in dbHandler.getSubscribedLocations()) {
                // Output forecast
                for (chatIdForUser in dbHandler.getSubscriptions()) {
                    sendAllForecastsToUser(chatIdForUser.key, chatIdForUser.value, LocalDate.now())
                }
            }
        }    }

    /**
     * Send daily weather update if it is time to do so
     */
    private fun trySendDailyWeatherUpdate() {
        val updateTime = LocalTime.of(7, 0)
        if (LocalTime.now() == updateTime) {
            for (location in dbHandler.getSubscribedLocations()) {
                val weather = weatherApi
                        .getCurrentWeather(Location.AVAILABLE_LOCATIONS[location] ?: error("Location unknown"))
                // Output weather data
                for (chatIdForUser in dbHandler.getSubscriptions()) {
                    sendWeatherUpdate(chatIdForUser.value, weather)
                }
            }
        }
    }

    private fun sendForecastUpdate(chatID: Int, forecast: Forecast) {
        val forecastText = forecast.getReportString()
        api.sendMessage(chatID, forecastText)
    }

    private fun sendWeatherUpdate(chatID: Int, currentWeather: CurrentWeather) {
        val weatherText = "##### Today's weather report for ${currentWeather.cityName} #####\n" +
                "${currentWeather.getWeatherReportString()}\n"
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
        dbHandler.updateLastReceivedUpdate(update.updateId, getCurrentEpoch())
    }

    private fun getCurrentEpoch(): Long {
        return System.currentTimeMillis() / 1000L
    }

    private fun handleMessage(message: Message) {
        when(message.text) {
            "/start" -> {
                sendMessage(message.chat, subscribeUser(message.user, message.chat))
            }
            "/stop" -> {
                sendMessage(message.chat, unsubscribeUser(message.user))
            }
            "weather" -> {
                for (location in dbHandler.getSubscribedLocationsForUser(message.user.id)) {
                    val weather = weatherApi
                            .getCurrentWeather(Location.AVAILABLE_LOCATIONS[location] ?: error("Location unknown"))
                    // Output weather data
                    sendWeatherUpdate(message.chat.id, weather)
                }
            }
            "forecast", "forecast today" -> {
                sendAllForecastsToUser(message.user.id, message.chat.id, LocalDate.now())
            }
            "forecast tomorrow" -> {
                sendAllForecastsToUser(message.user.id, message.chat.id, LocalDate.now().plusDays(1))
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

    private fun sendAllForecastsToUser(userID: Int, chatID: Int, forecastDate: LocalDate) {
        for (location in dbHandler.getSubscribedLocationsForUser(userID)) {
            val forecast = weatherApi
                    .getFiveDaysForecast(Location.AVAILABLE_LOCATIONS[location] ?: error("Location unknown"))
            // Output weather data
            sendWholeDayForecast(chatID, forecast, forecastDate)
        }
    }

    private fun sendWholeDayForecast(chatID: Int, fiveDaysForecast: FiveDaysForecast, targetDate: LocalDate) {
        val morningTime = LocalDateTime.of(targetDate, LocalTime.of(9, 0))
        val afternoonTime = LocalDateTime.of(targetDate, LocalTime.of(15, 0))
        val eveningTime = LocalDateTime.of(targetDate, LocalTime.of(21, 0))
        val nightTime = LocalDateTime.of(targetDate.plusDays(1), LocalTime.of(0, 0))

        val forecastTimes = listOf(morningTime, afternoonTime, eveningTime, nightTime)

        api.sendMessage(chatID, "##### Forecast report for $targetDate for ${fiveDaysForecast.cityName} #####\n")

        for (forecast in fiveDaysForecast.forecasts.filter { forecastTimes.contains(it.dateTime) }) {
            sendForecastUpdate(chatID, forecast)
        }
    }

    private fun addUserLocation(user: User, locationName: String): String {
        val location = Location.AVAILABLE_LOCATIONS.getValue(locationName)

        val subscribedLocations = dbHandler.getSubscribedLocationsForUser(user.id)
        if (subscribedLocations.contains(location.city))
            return "You are already subscribed to weather information for ${location.city}"

        dbHandler.addSubscribedLocation(user.id, location.city)
        return "You will now receive weather information for $location"
    }

    private fun startLocationChoiceDialog(chat: Chat) {
        val buttons: MutableList<Array<KeyboardButton>> = mutableListOf()
        for (availableLocation in Location.AVAILABLE_LOCATIONS) {
            val buttonRow = Array(1) { KeyboardButton(availableLocation.key) }
            buttons.add(buttonRow)
        }

        sendMessage(chat, "Please choose a location", ReplyKeyboardMarkup(buttons.toTypedArray(), true))
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
        if (dbHandler.getSubscriptions().any { it.key == user.id }) {
            val locations = dbHandler.getSubscribedLocationsForUser(user.id)
            val locationsString = locations.joinToString(", ")
            return "You have already subscribed to the following locations: $locationsString\n" +
                    "To subscribe to a new location, type its name in the chat.\n" +
                    "Available locations: " + Location.AVAILABLE_LOCATIONS.keys.joinToString(", ")
        }

        dbHandler.addUser(user)
        dbHandler.addSubscription(user.id, chat.id)
        startLocationChoiceDialog(chat)
        return "You have successfully subscribed to Dresden's best weather bot. Welcome aboard!"
    }
}