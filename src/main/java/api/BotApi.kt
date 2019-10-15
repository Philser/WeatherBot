package api

import api.model.Update
import khttp.get

class BotApi(apiToken: String) {

    val BASE_URL = "https://api.telegram.org/bot$apiToken/"

    fun getUpdates(): List<Update> {
        val url = BASE_URL + "getUpdates"
        val response = get(url).jsonObject

        return listOf()
    }
}