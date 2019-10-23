package test.philser.bot.db

import io.mockk.*
import main.philser.bot.db.SQLiteDBHandler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.sql.*
import kotlin.test.assertTrue

class SQLiteDBHandlerTest {
    private val dbName = "testDB"
    private val dbHandler: SQLiteDBHandler = SQLiteDBHandler(dbName)
    private val requiredTables = listOf("USER", "CHAT", "SUBSCRIPTION", "SUBSCRIBED_LOCATIONS",
            "LAST_WEATHER_UPDATE_TIME", "LAST_RECEIVED_UPDATE")
    private val dbConnectionName = "jdbc:sqlite:$dbName.db"

    val mockConn = mockk<Connection>()
    val mockStmt = mockk<PreparedStatement>()

    init {
        mockkStatic(DriverManager::class)
        every {DriverManager.getConnection(any())} returns mockConn
        every {mockConn.createStatement()} returns mockStmt
        every {mockStmt.close()} answers {}
        every {mockConn.close()} answers {}
    }

    @Test
    fun `All tables are initialized`() {
        // Arrange
        val queryList = mutableListOf<String>()
        every {mockStmt.executeUpdate(capture(queryList))} returns 1

        // Act
        dbHandler.createMissingTables()

        // Assert
        verify(exactly = 5) { mockStmt.executeUpdate(any()) }
        assertTrue { queryList.any {it.contains("CREATE TABLE IF NOT EXISTS USER")} }
        assertTrue { queryList.any {it.contains("CREATE TABLE IF NOT EXISTS CHAT")} }
        assertTrue { queryList.any {it.contains("CREATE TABLE IF NOT EXISTS SUBSCRIPTION")} }
        assertTrue { queryList.any {it.contains("CREATE TABLE IF NOT EXISTS SUBSCRIBED_LOCATIONS")} }
        assertTrue { queryList.any {it.contains("CREATE TABLE IF NOT EXISTS LAST_RECEIVED_UPDATE")} }
    }
}