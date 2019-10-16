package philser

import philser.bot.Bot
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size < 1) {
        println("Error: No token provided")
        exitProcess(1)
    }
    val apiToken = args[0]

    val bot = Bot(apiToken)
    bot.runBot()
}
