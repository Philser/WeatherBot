package philser.api.telegram.model

import org.json.JSONObject

class User {

    val id: Int
    val isBot: Boolean
    val userName: String

    constructor(userObject: JSONObject) {
        id = userObject["id"] as Int
        isBot = userObject["is_bot"] as Boolean
        userName = userObject["username"] as String
    }

    constructor(id: Int, isBot: Boolean, userName: String) {
        this.id = id
        this.isBot = isBot
        this.userName = userName
    }

}