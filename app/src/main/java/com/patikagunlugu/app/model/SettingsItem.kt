package com.patikagunlugu.app.model

data class SettingsItem(
    val id: Int,
    val title: String,
    val subtitle: String? = null,
    val iconRes: Int,
    val type: Type = Type.REGULAR
) {
    enum class Type {
        REGULAR, SWITCH, LANGUAGE, THEME
    }
}
