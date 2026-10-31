package com.example.keyboard.model

enum class ShiftState {
    LOWERCASE,
    SHIFTED,
    CAPS_LOCKED;

    fun isCapitalized(): Boolean = this != LOWERCASE
}

enum class KeyboardPage {
    LETTERS,
    NUMBERS,
    SYMBOLS,
    EXTENSIONS,
    EMOJI
}

data class KeyModel(
    val primaryText: String,
    val secondaryText: String? = null,
    val shiftText: String? = null,
    val code: Int = 0,
    val weight: Float = 1.0f,
    val isSpecial: Boolean = false,
    val actionType: KeyActionType = KeyActionType.CHARACTER
)

enum class KeyActionType {
    CHARACTER,
    SHIFT,
    BACKSPACE,
    ENTER,
    SWITCH_PAGE,
    SPACE,
    EMOJI_TOGGLE,
    EXTENSIONS_TOGGLE,
    HIDE_KEYBOARD
}
