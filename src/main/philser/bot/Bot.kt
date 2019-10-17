package philser.bot

import philser.api.BotApi
import philser.api.model.Chat
import philser.api.model.Message
import philser.api.model.Update
import philser.api.model.User

class Bot(apiToken: String) {

    var api: BotApi = BotApi(apiToken)
    var lastProcessedUpdate: Int = 0
    val subscribedUsers: HashMap<Int, User> = HashMap()

    fun runBot() {
        while (true) {
            // Poll for updates
            val updates: List<Update> = pollForUpdates()

            // Process updates
            handleUpdates(updates)

            // Fetch weather data
            

            // Output weather data

        }
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
                sendMessage(message.chat, subscribeUser(message.user))
            }
            "/stop" -> {
                sendMessage(message.chat, unsubscribeUser(message.user))
            }
            else -> {
                sendMessage(message.chat, "I do not understand this command.")
            }
        }
    }

    private fun sendMessage(chat: Chat, message: String) {
        val response = api.sendMessage(chat.id, message)
    }

    private fun unsubscribeUser(user: User): String {
        if (!subscribedUsers.containsKey(user.id))
            return "You haven't even subscribed yet!"

        subscribedUsers.remove(user.id)
        return "You have successfully unsubscribed from Dresden's best weather bot. Sad to see you leave!"
    }

    private fun subscribeUser(user: User): String {
        if (subscribedUsers.containsKey(user.id))
            return "You are already subscribed, silly!"

        subscribedUsers[user.id] = user
        return "You have successfully subscribed to Dresden's best weather bot. Welcome aboard!"
    }
}