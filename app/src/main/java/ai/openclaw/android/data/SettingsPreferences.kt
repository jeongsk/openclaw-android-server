package ai.openclaw.android.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore를 이용한 설정 저장소
 * API 키, 포트, 자동 시작 등 설정을 안전하게 저장
 */
class SettingsPreferences(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

        // 설정 키
        val AUTO_START_KEY = booleanPreferencesKey("auto_start")
        val GATEWAY_PORT_KEY = intPreferencesKey("gateway_port")
        val ANTHROPIC_API_KEY_KEY = stringPreferencesKey("anthropic_api_key")
        val OPENAI_API_KEY_KEY = stringPreferencesKey("openai_api_key")
        val TELEGRAM_BOT_TOKEN_KEY = stringPreferencesKey("telegram_bot_token")

        // 기본값
        const val DEFAULT_PORT = 18789
        const val DEFAULT_AUTO_START = false
    }

    /**
     * 자동 시작 설정 Flow
     */
    val autoStartEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_START_KEY] ?: DEFAULT_AUTO_START
    }

    /**
     * Gateway 포트 Flow
     */
    val gatewayPort: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[GATEWAY_PORT_KEY] ?: DEFAULT_PORT
    }

    /**
     * Anthropic API Key Flow
     */
    val anthropicApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ANTHROPIC_API_KEY_KEY] ?: ""
    }

    /**
     * OpenAI API Key Flow
     */
    val openaiApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[OPENAI_API_KEY_KEY] ?: ""
    }

    /**
     * Telegram Bot Token Flow
     */
    val telegramBotToken: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TELEGRAM_BOT_TOKEN_KEY] ?: ""
    }

    /**
     * 자동 시작 설정 변경
     */
    suspend fun setAutoStart(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_START_KEY] = enabled
        }
    }

    /**
     * Gateway 포트 변경
     */
    suspend fun setGatewayPort(port: Int) {
        context.dataStore.edit { preferences ->
            preferences[GATEWAY_PORT_KEY] = port
        }
    }

    /**
     * Anthropic API Key 저장
     */
    suspend fun setAnthropicApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[ANTHROPIC_API_KEY_KEY] = key
        }
    }

    /**
     * OpenAI API Key 저장
     */
    suspend fun setOpenaiApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[OPENAI_API_KEY_KEY] = key
        }
    }

    /**
     * Telegram Bot Token 저장
     */
    suspend fun setTelegramBotToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TELEGRAM_BOT_TOKEN_KEY] = token
        }
    }

    /**
     * 모든 설정 초기화
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * API 키가 하나라도 설정되어 있는지 확인
     */
    fun hasAnyApiKey(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        !preferences[ANTHROPIC_API_KEY_KEY].isNullOrEmpty() ||
        !preferences[OPENAI_API_KEY_KEY].isNullOrEmpty()
    }
}
