package main.philser.bot.db

import philser.api.telegram.model.Chat
import philser.api.telegram.model.User

interface DBHandler {

    fun addUser(user: User)
    fun addChat(chat: Chat)
    fun addSubscription(userID: Int, chatID: Int)
    fun addSubscribedLocation(userID: Int, city: String)
    fun upsertLastWeatherUpdateTime(userID: Int, unixTimestamp: Long)
    fun updateLastReceivedUpdate(updateID: Int, unixTimestamp: Long)

    fun getUsers(): List<User>
    fun getChats(): List<Chat>
    fun getSubscriptions(): Map<Int, Int>
    fun getSubscriptionByUserID(userID: Int): Pair<Int, Int>
    fun getSubscribedLocationsPerUsers(): Map<Int, List<String>>
    fun getSubscribedLocations(): List<String>
    fun getSubscribedLocationsForUser(userID: Int): List<String>
    fun getLastWeatherUpdateTimeForUser(userID: Int): Int
    fun getLastReceivedUpdateIDAndTime(): Pair<Int, Int>?

    fun deleteUser(userID: Int)
    fun deleteChat(chatID: Int)
    fun deleteSubscription(userID: Int, chatID: Int)
    fun deleteSubscribedLocation(userID: Int, location: String)
    fun deleteAllSubscribedLocationsForUser(userID: Int)
    fun deleteLastWeatherUpdateTimeForUser(userID: Int)
}