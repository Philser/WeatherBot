package philser.api.telegram

import philser.api.telegram.model.Update
import khttp.get
import khttp.post
import main.philser.api.telegram.model.ReplyKeyboardMarkup
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.LoggerFactory
import philser.api.telegram.model.Message
import philser.logger

class BotApi(apiToken: String) {

    private val BASE_URL = "https://api.telegram.org/bot$apiToken/"
    private val logger by logger()

    fun getUpdates(): List<Update> {
        val url = BASE_URL + "getUpdates"
        val response = sendGet(url)
        val updates: JSONArray = response["result"] as JSONArray

        return updates.map { Update(it as JSONObject) }
    }

    // TODO: Include Markdown and HTML parsing modes
    fun sendMessage(chatId: Int, message: String, replyMarkup: ReplyKeyboardMarkup? = null): Message {
        val url = BASE_URL + "sendMessage"
        var payload = mutableMapOf("chat_id" to chatId, "text" to message)
        if (replyMarkup != null) payload["reply_markup"] = replyMarkup

        val response = sendPost(url, JSONObject(payload))
        return Message(response["result"] as JSONObject)
    }

    fun sendPost(url: String, payload: JSONObject): JSONObject {
        logger.debug("Sending POST to $url")
        val response = post(url, json=payload).jsonObject
        if (response["ok"] != true )
            throw Exception("Request failed: " + response["error_code"] + ": " + response["description"])

        return response
    }

    fun sendGet(url: String): JSONObject {
        logger.debug("Sending GET to $url")
        val response = get(url).jsonObject
        if (response["ok"] != true )
            throw Exception("Request failed: " + response["error_code"] + ": " + response["description"])

        return response
    }
}