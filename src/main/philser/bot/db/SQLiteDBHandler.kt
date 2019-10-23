package main.philser.bot.db

import main.philser.bot.db.DBHandler
import philser.api.telegram.model.Chat
import philser.api.telegram.model.User
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement


class SQLiteDBHandler(dbName: String) : DBHandler {

    private val DB_CONNECTION_NAME: String

    private val USER_TABLE_NAME = "USER"
    private val SUBSCRIPTION_TABLE_NAME = "SUBSCRIPTION"
    private val CHAT_TABLE_NAME = "CHAT"
    private val SUBSRIBED_LOCATIONS_TABLE_NAME = "SUBSCRIBED_LOCATIONS"
    private val LAST_WEATHER_UPDATE_TIME_TABLE_NAME = "LAST_WEATHER_UPDATE_TIME"
    private val LAST_RECEIVED_UPDATE_MESSAGE_TABLE_NAME = "LAST_RECEIVED_UPDATE"

    init {
        // Load JDBC driver
        DB_CONNECTION_NAME = "jdbc:sqlite:$dbName.db"
        try {
            Class.forName("org.sqlite.JDBC") // Load driver
        } catch (e: Exception) {
            System.err.println("Error loading JDBC driver: " + e.message)
            System.exit(0)
        }
    }

    fun createMissingTables() {
        createUserTable()
        createChatTable()
        createSubscriptionTable()
        createSubscribedLocationsTable()
        createLastReceivedUpdateMessageTable()
    }

    private fun createLastReceivedUpdateMessageTable() {
        val query = "CREATE TABLE IF NOT EXISTS $LAST_RECEIVED_UPDATE_MESSAGE_TABLE_NAME" +
                "(UPDATE_ID INTEGER PRIMARY KEY," +
                " TIME INTEGER)"
        executeUpdate(query)
    }

    private fun createLastWeatherUpdateTable() {
        val query = "CREATE TABLE IF NOT EXISTS $LAST_WEATHER_UPDATE_TIME_TABLE_NAME" +
                "(USER_ID INTEGER PRIMARY KEY," +
                " TIME INTEGER," +
                " FOREIGN KEY(USER_ID) REFERENCES $USER_TABLE_NAME(ID))"
        executeUpdate(query)
    }

    private fun createSubscribedLocationsTable() {
        val query = "CREATE TABLE IF NOT EXISTS $SUBSRIBED_LOCATIONS_TABLE_NAME" +
                "(USER_ID INTEGER NOT NULL," +
                " LOCATION         TEXT    NOT NULL, " +
                " PRIMARY KEY(USER_ID, LOCATION)," +
                " FOREIGN KEY(USER_ID) REFERENCES $USER_TABLE_NAME(ID))"
        executeUpdate(query)
    }

    private fun createChatTable() {
        val query = "CREATE TABLE IF NOT EXISTS $CHAT_TABLE_NAME" +
                "(ID INTEGER PRIMARY KEY     NOT NULL," +
                " TITLE         TEXT    NOT NULL, " +
                " FIRST_NAME      TEXT     NOT NULL," +
                " LAST_NAME      TEXT     NOT NULL)"
        executeUpdate(query)
    }

    private fun createUserTable() {
        val query = "CREATE TABLE IF NOT EXISTS $USER_TABLE_NAME " +
                "(ID INTEGER PRIMARY KEY     NOT NULL," +
                " IS_BOT         BOOLEAN    NOT NULL, " +
                " USER_NAME      TEXT     NOT NULL)"
        executeUpdate(query)
    }

    private fun createSubscriptionTable() {
        val query = "CREATE TABLE IF NOT EXISTS $SUBSCRIPTION_TABLE_NAME " +
                "(USER_ID INTEGER," +
                " CHAT_ID INTEGER," +
                " PRIMARY KEY(USER_ID, CHAT_ID)," +
                " FOREIGN KEY(USER_ID) REFERENCES $USER_TABLE_NAME(ID)," +
                " FOREIGN KEY(CHAT_ID) REFERENCES $CHAT_TABLE_NAME(ID))"
        executeUpdate(query)
    }

    private fun executeUpdate(query: String) {
        val c = DriverManager.getConnection(DB_CONNECTION_NAME)

        val stmt = c.createStatement()

        stmt.executeUpdate(query)
        stmt.close()
        c.close()
    }

    override fun addUser(user: User) {
        val query = "INSERT INTO $USER_TABLE_NAME" +
                " VALUES(${user.id}, ${user.isBot}, '${user.userName}')"
        executeUpdate(query)
    }

    override fun addChat(chat: Chat) {
        val query = "INSERT INTO $CHAT_TABLE_NAME" +
                " VALUES (${chat.id}, '${chat.title}', '${chat.firstName}', '${chat.lastName}')"
        executeUpdate(query)
    }

    override fun addSubscription(userID: Int, chatID: Int) {
        val query = "INSERT INTO $SUBSCRIPTION_TABLE_NAME" +
                " VALUES ($userID, $chatID)"
        executeUpdate(query)
    }

    override fun addSubscribedLocation(userID: Int, city: String) {
        val query = "INSERT INTO $SUBSRIBED_LOCATIONS_TABLE_NAME" +
                " VALUES ($userID, '$city')"
        executeUpdate(query)
    }

    override fun upsertLastWeatherUpdateTime(userID: Int, unixTimestamp: Long) {
        val query = "INSERT INTO $LAST_WEATHER_UPDATE_TIME_TABLE_NAME" +
                " VALUES($userID, $unixTimestamp)" +
                " ON CONFLICT(USER_ID)" +
                " DO UPDATE SET TIME = excluded.TIME"
        executeUpdate(query)
    }

    override fun updateLastReceivedUpdate(updateID: Int, unixTimestamp: Long) {
        val queryDeleteOld = "DELETE FROM $LAST_RECEIVED_UPDATE_MESSAGE_TABLE_NAME"
        val queryAddNew = "INSERT INTO $LAST_RECEIVED_UPDATE_MESSAGE_TABLE_NAME VALUES($updateID, $unixTimestamp)"
        executeUpdate(queryDeleteOld)
        executeUpdate(queryAddNew)
    }

    override fun getUsers(): List<User> {
        val conn = DriverManager.getConnection(DB_CONNECTION_NAME)
        val stmt: Statement = conn.createStatement()
        val query = "SELECT * FROM $USER_TABLE_NAME"
        try {
            val results = stmt.executeQuery(query)
            val users: MutableList<User> = mutableListOf()
            while(results.next()) {
                val id = results.getInt("ID")
                val isBot = results.getBoolean("IS_BOT")
                val userName = results.getString("USER_NAME")
                users.add(User(id, isBot, userName))
            }

            return users
        } finally {
            conn.close()
            stmt.close()
        }
    }

    override fun getChats(): List<Chat> {
        val conn = DriverManager.getConnection(DB_CONNECTION_NAME)
        val stmt: Statement = conn.createStatement()
        val query = "SELECT ID, IS_BOT, USER_NAME FROM $CHAT_TABLE_NAME;"
        try {
            val results = stmt.executeQuery(query)
            val chats: MutableList<Chat> = mutableListOf()
            while(results.next()) {
                val id = results.getInt("ID")
                val title = results.getString("TITLE")
                val firstName = results.getString("FIRST_NAME")
                val lastName = results.getString("LAST_NAME")
                chats.add(Chat(id, title, firstName, lastName))
            }

            return chats
        } finally {
            conn.close()
            stmt.close()
        }
    }

    override fun getSubscriptions(): Map<Int, Int> {
        val conn = DriverManager.getConnection(DB_CONNECTION_NAME)
        val stmt: Statement = conn.createStatement()
        val query = "SELECT USER_ID, CHAT_ID FROM $SUBSCRIPTION_TABLE_NAME"
        try {
            val results = stmt.executeQuery(query)
            val subscriptions: HashMap<Int, Int> = HashMap()
            while(results.next()) {
                val userId = results.getInt("USER_ID")
                val chatId = results.getInt("CHAT_ID")
                subscriptions[userId] = chatId
            }

            return subscriptions
        } finally {
            conn.close()
            stmt.close()
        }
    }

    override fun getSubscriptionByUserID(userID: Int): Pair<Int, Int> {
        val conn = DriverManager.getConnection(DB_CONNECTION_NAME)
        val stmt: Statement = conn.createStatement()
        val query = "SELECT USER_ID, CHAT_ID FROM $SUBSCRIPTION_TABLE_NAME WHERE USER_ID = $userID"
        try {
            val results = stmt.executeQuery(query)
            while(results.next()) {
                val userId = results.getInt("USER_ID")
                val chatId = results.getInt("CHAT_ID")
                return Pair(userId, chatId)
            }
            throw Exception("Subscription does not exist")
        } finally {
            conn.close()
            stmt.close()
        }
    }

    override fun getSubscribedLocationsPerUsers(): Map<Int, List<String>> {
        val conn = DriverManager.getConnection(DB_CONNECTION_NAME)
        val stmt: Statement = conn.createStatement()
        val query = "SELECT USER_ID, CITY FROM $SUBSRIBED_LOCATIONS_TABLE_NAME"
        try {
            val results = stmt.executeQuery(query)
            val subscribedLocations: HashMap<Int, MutableList<String>> = HashMap()
            while(results.next()) {
                val userId = results.getInt("USER_ID")
                val city = results.getString("CITY")
                if(!subscribedLocations.containsKey(userId))
                    subscribedLocations[userId] = mutableListOf()
                subscribedLocations[userId]!!.add(city)
            }

            return subscribedLocations
        } finally {
            conn.close()
            stmt.close()
        }
    }

    override fun getSubscribedLocations(): List<String> {
        val conn = DriverManager.getConnection(DB_CONNECTION_NAME)
        val stmt: Statement = conn.createStatement()
        val query = "SELECT DISTINCT LOCATION FROM $SUBSRIBED_LOCATIONS_TABLE_NAME"
        try {
            val results = stmt.executeQuery(query)
            val subscribedLocations: MutableList<String> = mutableListOf()
            while(results.next()) {
                val location = results.getString("LOCATION")
                subscribedLocations.add(location)
            }

            return subscribedLocations
        } finally {
            conn.close()
            stmt.close()
        }
    }

    override fun getSubscribedLocationsForUser(userID: Int): List<String> {
        val conn = DriverManager.getConnection(DB_CONNECTION_NAME)
        val stmt: Statement = conn.createStatement()
        val query = "SELECT LOCATION FROM $SUBSRIBED_LOCATIONS_TABLE_NAME WHERE USER_ID = $userID"
        try {
            val results = stmt.executeQuery(query)
            val locations = mutableListOf<String>()
            while (results.next()) {
                locations.add(results.getString("LOCATION"))
            }

            return locations
        } finally {
            conn.close()
            stmt.close()
        }
    }

    override fun getLastReceivedUpdateIDAndTime(): Pair<Int, Int>? {
        val conn = DriverManager.getConnection(DB_CONNECTION_NAME)
        val stmt: Statement = conn.createStatement()
        val query = "SELECT UPDATE_ID, TIME FROM $LAST_RECEIVED_UPDATE_MESSAGE_TABLE_NAME ORDER BY TIME DESC LIMIT 1"
        try {
            val result = stmt.executeQuery(query)
            if(result.next()) {
                val updateID = result.getInt("UPDATE_ID")
                val time = result.getInt("TIME")
                return Pair(updateID, time)
            }

            return null
        } finally {
            conn.close()
            stmt.close()
        }
    }

    override fun getLastWeatherUpdateTimeForUser(userID: Int): Int {
        val conn = DriverManager.getConnection(DB_CONNECTION_NAME)
        val stmt: Statement = conn.createStatement()
        val query = "SELECT TIME FROM $LAST_WEATHER_UPDATE_TIME_TABLE_NAME WHERE USER_ID = $userID"
        try {
            val result = stmt.executeQuery(query)
            if (result.next())
                return result.getInt("TIME")
            return 0
        } finally {
            conn.close()
            stmt.close()
        }

    }

    override fun deleteUser(userID: Int) {
        val query = "DELETE FROM $USER_TABLE_NAME WHERE ID=$userID"
        executeUpdate(query)
    }

    override fun deleteChat(chatID: Int) {
        val query = "DELETE FROM $CHAT_TABLE_NAME WHERE ID=$chatID"
        executeUpdate(query)
    }

    override fun deleteSubscription(userID: Int, chatID: Int) {
        val query = "DELETE FROM $SUBSCRIPTION_TABLE_NAME WHERE USER_ID=$userID AND CHAT_ID=$chatID"
        executeUpdate(query)
    }

    override fun deleteSubscribedLocation(userID: Int, location: String) {
        val query = "DELETE FROM $SUBSRIBED_LOCATIONS_TABLE_NAME WHERE USER_ID=$userID AND LOCATION LIKE '$location'"
        executeUpdate(query)
    }

    override fun deleteAllSubscribedLocationsForUser(userID: Int) {
        val query = "DELETE FROM $SUBSRIBED_LOCATIONS_TABLE_NAME WHERE USER_ID=$userID"
        executeUpdate(query)
    }

    override fun deleteLastWeatherUpdateTimeForUser(userID: Int) {
        val query = "DELETE FROM $LAST_WEATHER_UPDATE_TIME_TABLE_NAME WHERE USER_ID=$userID"
        executeUpdate(query)
    }
}