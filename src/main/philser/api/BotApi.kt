package philser.api

import philser.api.model.Update
import khttp.get
import org.json.JSONArray
import org.json.JSONObject

class BotApi(apiToken: String) {

    val BASE_URL = "https://api.telegram.org/bot$apiToken/"

    fun getUpdates(): List<Update> {
        val url = BASE_URL + "getUpdates"
        val response = sendRequest(url)
        val updates: JSONArray = response["result"] as JSONArray

        return updates.map { Update(it as JSONObject) }
    }

    fun sendRequest(url: String): JSONObject {
        val response = get(url).jsonObject
        if (response["ok"] != true )
            throw Exception("Request failed: " + response["error_code"] + ": " + response["description"])

        return response
    }
}