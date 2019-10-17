package philser.api.telegram.model

import org.json.JSONObject

class User(userObject: JSONObject) {

    val id: Int = userObject["id"] as Int
    val isBot: Boolean = userObject["is_bot"] as Boolean
    val userName: String = userObject["username"] as String


}