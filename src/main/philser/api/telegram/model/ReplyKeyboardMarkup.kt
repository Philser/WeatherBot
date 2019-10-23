package main.philser.api.telegram.model

class ReplyKeyboardMarkup(buttons: Array<Array<KeyboardButton>>, hideAfterFirstUsage: Boolean) {

    val keyboard: Array<Array<KeyboardButton>> = buttons
    val one_time_keyboard: Boolean = hideAfterFirstUsage
}