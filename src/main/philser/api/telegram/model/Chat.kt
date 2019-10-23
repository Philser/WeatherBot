package philser.api.telegram.model

import org.json.JSONObject

class Chat {

    val title: String?
    val id: Int
    val firstName: String?
    val lastName: String?

    constructor(chatObject: JSONObject) {
        id = chatObject["id"] as Int
        title = if (chatObject.has("title")) chatObject["title"] as String else null
        firstName = if (chatObject.has("first_name")) chatObject["first_name"] as String else null
        lastName = if (chatObject.has("last_name")) chatObject["last_name"] as String else null
    }

    constructor(id: Int, title: String, firstName: String?, lastName: String?) {
        this.id = id
        this.title = title
        this.firstName = firstName
        this.lastName = lastName
    }
}
