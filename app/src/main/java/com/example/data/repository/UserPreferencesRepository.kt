package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import com.example.data.model.AiModel
import com.example.data.model.ThemeMode
import com.example.data.model.UserTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "light_gpt_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
        val SELECTED_MODEL = stringPreferencesKey("selected_model")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USER_TIER = stringPreferencesKey("user_tier")
        val DAILY_USAGE_COUNT = intPreferencesKey("daily_usage_count")
        val LAST_USAGE_DATE = stringPreferencesKey("last_usage_date")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_BIO = stringPreferencesKey("user_bio")
        val USER_AVATAR_BASE64 = stringPreferencesKey("user_avatar_base64")
    }

    val customApiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CUSTOM_API_KEY]?.takeIf { it.isNotBlank() }
    }

    val selectedModel: Flow<AiModel> = context.dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.SELECTED_MODEL]
        AiModel.values().firstOrNull { it.modelId == raw } ?: AiModel.FLASH_3_5
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.THEME_MODE]
        ThemeMode.values().firstOrNull { it.name == raw } ?: ThemeMode.DARK
    }

    val userTier: Flow<UserTier> = context.dataStore.data.map { preferences ->
        val raw = preferences[PreferencesKeys.USER_TIER]
        UserTier.values().firstOrNull { it.name == raw } ?: UserTier.FREE
    }

    val dailyUsageCount: Flow<Int> = context.dataStore.data.map { preferences ->
        val today = getTodayString()
        val lastDate = preferences[PreferencesKeys.LAST_USAGE_DATE]
        if (lastDate == today) {
            preferences[PreferencesKeys.DAILY_USAGE_COUNT] ?: 0
        } else {
            0
        }
    }

    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_NAME] ?: "Explorer"
    }

    val userEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_EMAIL] ?: "faisaldummar009@gmail.com"
    }

    val userBio: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_BIO] ?: "Exploring the frontiers of intelligence with Light GPT 🚀"
    }

    val userAvatarBase64: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_AVATAR_BASE64]
    }

    /**
     * Resolves the active Gemini API key:
     * Priority: User custom API key in settings -> BuildConfig.GEMINI_API_KEY
     */
    fun getEffectiveApiKey(customKey: String?): String {
        if (!customKey.isNullOrBlank()) return customKey.trim()
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    suspend fun setCustomApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_API_KEY] = key.trim()
        }
    }

    suspend fun setSelectedModel(model: AiModel) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_MODEL] = model.modelId
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setUserTier(tier: UserTier) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_TIER] = tier.name
        }
    }

    suspend fun incrementUsage() {
        val today = getTodayString()
        context.dataStore.edit { preferences ->
            val lastDate = preferences[PreferencesKeys.LAST_USAGE_DATE]
            val current = if (lastDate == today) {
                preferences[PreferencesKeys.DAILY_USAGE_COUNT] ?: 0
            } else {
                0
            }
            preferences[PreferencesKeys.LAST_USAGE_DATE] = today
            preferences[PreferencesKeys.DAILY_USAGE_COUNT] = current + 1
        }
    }

    suspend fun updateProfile(name: String, email: String, bio: String, avatarBase64: String?) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name.trim()
            preferences[PreferencesKeys.USER_EMAIL] = email.trim()
            preferences[PreferencesKeys.USER_BIO] = bio.trim()
            if (avatarBase64 != null) {
                preferences[PreferencesKeys.USER_AVATAR_BASE64] = avatarBase64
            }
        }
    }

    private fun getTodayString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }
}
