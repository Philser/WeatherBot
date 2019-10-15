package bot

import api.BotApi
import api.model.Update

class Bot {

    lateinit var api: BotApi

    constructor(apiToken: String) {
        api = BotApi(apiToken)
    }

    fun runBot() {
        while (true) {
            // Poll for updates
            var updates: List<Update> = pollForUpdates()

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

    }
}