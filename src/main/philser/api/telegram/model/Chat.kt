package philser.api.telegram.model

import org.json.JSONObject

class Chat(chatObject: JSONObject) {
    val id = chatObject["id"] as Int
    val title = if (chatObject.has("title")) chatObject["title"] as String else null
    val firstName = if (chatObject.has("first_name")) chatObject["first_name"] as String else null
    val lastName = if (chatObject.has("last_name")) chatObject["last_name"] as String else null
}
