package test.philser.bot.db

import main.philser.bot.db.SQLiteDBHandler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.sql.Connection
import java.sql.DriverManager

class SQLiteDBHandlerTest {
    private val dbName = "testDB"
    private val dbHandler: SQLiteDBHandler = SQLiteDBHandler(dbName)
    private val requiredTables = listOf("USER", "CHAT", "SUBSCRIPTION", "SUBSCRIBED_LOCATIONS",
            "LAST_WEATHER_UPDATE_TIME", "LAST_RECEIVED_UPDATE")
    private val dbConnectionName = "jdbc:sqlite:$dbName.db"

    init {
        // Init JDBC driver to check results
        try {
            Class.forName("org.sqlite.JDBC") // Load driver
        } catch (e: Exception) {
            System.err.println("Error loading JDBC driver: " + e.message)
            System.exit(0)
        }
    }

    @Test
    fun `Initializing DB Tables`() {
        dbHandler.createMissingTables()

        val dbConnection = DriverManager.getConnection(dbConnectionName)
        val queryTableExists = "SELECT name FROM sqlite_master"
        val stmt = dbConnection.prepareStatement(queryTableExists)
        val results = stmt.executeQuery()
        val createdTables: MutableList<String> = mutableListOf()
        while (results.next()) {
            createdTables.add(results.getString("name"))
        }
        stmt.close()
        dbConnection.close()

        for (table in requiredTables) {
            if (!createdTables.contains(table))
                fail("Table $table not created")
        }
    }
}