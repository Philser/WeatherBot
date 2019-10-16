package philser.api.model

import org.json.JSONObject

class Message(messageObject: JSONObject) {

    val messageId: Int = messageObject["message_id"] as Int
    val user: User = User(messageObject["from"] as JSONObject)
    val text: String? = if (messageObject.has("text")) messageObject["text"] as String else null
    val date: Int = messageObject["date"] as Int
    val chat: Chat = Chat(messageObject["chat"] as JSONObject)
}