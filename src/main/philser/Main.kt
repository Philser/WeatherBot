package philser

import main.philser.bot.Bot
import main.philser.bot.db.SQLiteDBHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.system.exitProcess

fun <T: Any> logger(forClass: Class<T>): Logger {
    return LoggerFactory.getLogger(unwrapCompanionClass(forClass).name)
}

// unwrap companion class to enclosing class given a Java Class
fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return ofClass.enclosingClass?.takeIf {
        ofClass.enclosingClass.kotlin.companionObject?.java == ofClass
    } ?: ofClass
}

// unwrap companion class to enclosing class given a Kotlin Class
fun <T: Any> unwrapCompanionClass(ofClass: KClass<T>): KClass<*> {
    return unwrapCompanionClass(ofClass.java).kotlin
}


// return a lazy logger property delegate for enclosing class
fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { logger(javaClass) }
}


// TODO Fetch tokens from config
fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Error: Please provide tokens for both the Telegram and the OpenWeatherMap API")
        exitProcess(1)
    }
    val apiToken = args[0]
    val weatherApiToken = args[1]

    val dbHandler = SQLiteDBHandler("db")
    dbHandler.createMissingTables()

    val bot = Bot(apiToken, weatherApiToken, dbHandler)
    bot.runBot()
}
