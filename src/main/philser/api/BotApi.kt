package philser.api

import philser.api.model.Update
import khttp.get
import khttp.post
import org.json.JSONArray
import org.json.JSONObject
import philser.api.model.Message

class BotApi(apiToken: String) {

    val BASE_URL = "https://api.telegram.org/bot$apiToken/"

    fun getUpdates(): List<Update> {
        val url = BASE_URL + "getUpdates"
        val response = sendGet(url)
        val updates: JSONArray = response["result"] as JSONArray

        return updates.map { Update(it as JSONObject) }
    }

    // TODO: Include Markdown and HTML parsing modes
    fun sendMessage(chatId: Int, message: String): Message {
        val url = BASE_URL + "sendMessage"
        val payload = JSONObject(mapOf("chat_id" to chatId, "text" to message))
        val response = sendPost(url, payload)
        return Message(response["result"] as JSONObject)
    }

    fun sendPost(url: String, payload: JSONObject): JSONObject {
        val response = post(url, json=payload).jsonObject
        if (response["ok"] != true )
            throw Exception("Request failed: " + response["error_code"] + ": " + response["description"])

        return response
    }

    fun sendGet(url: String): JSONObject {
        val response = get(url).jsonObject
        if (response["ok"] != true )
            throw Exception("Request failed: " + response["error_code"] + ": " + response["description"])

        return response
    }
}