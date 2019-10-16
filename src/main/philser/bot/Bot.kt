package philser.bot

import philser.api.BotApi
import philser.api.model.Chat
import philser.api.model.Message
import philser.api.model.Update
import philser.api.model.User

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

    // TODO: Handle other events?
    private fun handleUpdates(updates: List<Update>) {
        for (update in updates) {
            if (update.message != null)
                handleMessage(update.message)
        }
    }

    // TODO: Keep track of processed updates
    private fun handleMessage(message: Message) {
        when(message.text) {
            "/start" -> {
                subscribeUser(message.user)
                sendMessage(message.chat, "You have successfully subscribed to Dresden's best weather bot. Welcome aboard!")
            }
            "/stop" -> {
                unsubscribeUser(message.user)
                sendMessage(message.chat, "You have successfully unsubscribed from Dresden's best weather bot. Sad to see you leave!")
            }
            else -> {
                sendMessage(message.chat, "I do not understand this command.")
            }
        }
    }

    private fun sendMessage(chat: Chat, message: String) {
        val response = api.sendMessage(chat.id, message)
    }

    private fun unsubscribeUser(user: User) {

    }

    private fun subscribeUser(user: User) {

    }
}