package ai.openclaw.android.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "openclaw_settings")

/**
 * 앱 설정 관리 (DataStore 사용)
 */
class SettingsManager(private val context: Context) {
    
    companion object {
        // API 키
        private val ANTHROPIC_API_KEY = stringPreferencesKey("anthropic_api_key")
        private val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        
        // 채널 설정
        private val TELEGRAM_BOT_TOKEN = stringPreferencesKey("telegram_bot_token")
        private val DISCORD_BOT_TOKEN = stringPreferencesKey("discord_bot_token")
        
        // 일반 설정
        private val GATEWAY_PORT = stringPreferencesKey("gateway_port")
        private val AUTO_START = booleanPreferencesKey("auto_start")
        private val BATTERY_OPTIMIZATION_SHOWN = booleanPreferencesKey("battery_optimization_shown")
    }
    
    // API Keys
    val anthropicApiKey: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[ANTHROPIC_API_KEY] ?: "" }
    
    val openaiApiKey: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[OPENAI_API_KEY] ?: "" }
    
    // Channels
    val telegramBotToken: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[TELEGRAM_BOT_TOKEN] ?: "" }
    
    val discordBotToken: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[DISCORD_BOT_TOKEN] ?: "" }
    
    // General
    val gatewayPort: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[GATEWAY_PORT] ?: "18789" }
    
    val autoStart: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[AUTO_START] ?: false }
    
    val batteryOptimizationShown: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[BATTERY_OPTIMIZATION_SHOWN] ?: false }
    
    // Setters
    suspend fun setAnthropicApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[ANTHROPIC_API_KEY] = key
        }
    }
    
    suspend fun setOpenaiApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[OPENAI_API_KEY] = key
        }
    }
    
    suspend fun setTelegramBotToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TELEGRAM_BOT_TOKEN] = token
        }
    }
    
    suspend fun setDiscordBotToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[DISCORD_BOT_TOKEN] = token
        }
    }
    
    suspend fun setGatewayPort(port: String) {
        context.dataStore.edit { preferences ->
            preferences[GATEWAY_PORT] = port
        }
    }
    
    suspend fun setAutoStart(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_START] = enabled
        }
    }
    
    suspend fun setBatteryOptimizationShown(shown: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BATTERY_OPTIMIZATION_SHOWN] = shown
        }
    }
    
    // Clear all settings
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
