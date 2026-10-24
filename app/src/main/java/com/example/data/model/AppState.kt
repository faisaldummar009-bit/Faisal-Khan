package com.example.data.model

enum class ScreenDestination(val title: String, val icon: String) {
    CHAT("Chat", "ChatBubble"),
    IMAGE_GEN("AI Images", "AutoAwesome"),
    IMAGE_EDIT("Image Studio", "Brush"),
    AI_TOOLS("AI Tools", "Build")
}

enum class AiModel(val modelId: String, val displayName: String, val tag: String) {
    FLASH_3_5("gemini-3.5-flash", "Gemini 3.5 Flash", "Fast & Responsive"),
    PRO_3_1("gemini-3.1-pro-preview", "Gemini 3.1 Pro", "Deep Reasoning & Code"),
    IMAGE_FLASH("gemini-2.5-flash-image", "Gemini Image Studio", "Visual Synthesis")
}

enum class ThemeMode {
    DARK, LIGHT, SYSTEM
}

enum class UserTier(val title: String, val dailyLimit: Int) {
    FREE("Free Plan", 25),
    PRO("Light GPT Plus", 250)
}
