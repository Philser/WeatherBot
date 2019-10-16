package philser.bot

import philser.api.BotApi
import philser.api.model.Update

class Bot(apiToken: String) {

    var api: BotApi = BotApi(apiToken)

    fun runBot() {
        while (true) {
            // Poll for updates
            val updates: List<Update> = pollForUpdates()

            // Process updates
            handleUpdates(updates)

            // Allow subscriptions
            // Fetch weather data
            // Output weather data

        }
    }

    private fun pollForUpdates(): List<Update> {
        return api.getUpdates()
    }

    private fun handleUpdates(updates: List<Update>) {
        throw NotImplementedError()
    }
}