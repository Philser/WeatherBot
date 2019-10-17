package philser

import philser.bot.Bot
import kotlin.system.exitProcess

// TODO Fetch tokens from config
fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Error: Please provide tokens for both the Telegram and the OpenWeatherMap API")
        exitProcess(1)
    }
    val apiToken = args[0]
    val weatherApiToken = args[1]

    val bot = Bot(apiToken, weatherApiToken)
    bot.runBot()
}
