package philser.api.telegram.model

import org.json.JSONObject

class Update(updateObject: JSONObject) {

    val updateId: Int = updateObject["update_id"] as Int
    val message: Message? = if (updateObject.has("message")) Message(updateObject["message"] as JSONObject) else null

}