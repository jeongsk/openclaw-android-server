package ai.openclaw.android.server

import android.util.Log
import com.google.gson.Gson
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Duration

/**
 * Ktor 기반 OpenClaw Gateway 서버
 *
 * Android에서 직접 실행되는 경량 HTTP/WebSocket 서버입니다.
 * Node.js 의존성 없이 순수 Kotlin으로 구현되었습니다.
 */
class GatewayServer(private val port: Int = 18789) {

    companion object {
        const val TAG = "GatewayServer"
        private val gson = Gson()

        // 상태 Flow
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning

        private val _connectedClients = MutableStateFlow(0)
        val connectedClients: StateFlow<Int> = _connectedClients

        private val _messageCount = MutableStateFlow(0L)
        val messageCount: StateFlow<Long> = _messageCount
    }

    private var server: ApplicationEngine? = null
    private val activeSessions = mutableSetOf<WebSocketSession>()

    /**
     * 서버 시작
     */
    fun start(): Boolean {
        if (_isRunning.value) {
            Log.w(TAG, "Server already running")
            return true
        }

        return try {
            Log.i(TAG, "Starting Gateway server on port $port...")

            server = embeddedServer(Netty, port) {
                install(WebSockets) {
                    pingPeriod = Duration.ofSeconds(15)
                    timeout = Duration.ofSeconds(30)
                    maxFrameSize = Long.MAX_VALUE
                    masking = false
                }

                install(ContentNegotiation) {
                    gson {
                        setPrettyPrinting()
                        serializeNulls()
                    }
                }

                install(CORS) {
                    anyHost()
                    allowHeader(HttpHeaders.ContentType)
                    allowHeader(HttpHeaders.Authorization)
                    allowMethod(HttpMethod.Get)
                    allowMethod(HttpMethod.Post)
                    allowMethod(HttpMethod.Options)
                }

                routing {
                    // Health check
                    get("/health") {
                        call.respond(mapOf(
                            "status" to "ok",
                            "version" to "1.0.0",
                            "uptime" to System.currentTimeMillis(),
                            "clients" to synchronized(activeSessions) { activeSessions.size }
                        ))
                    }

                    // API info
                    get("/") {
                        call.respond(mapOf(
                            "name" to "OpenClaw Android Gateway",
                            "version" to "1.0.0",
                            "endpoints" to listOf(
                                "/health - Health check",
                                "/ws - WebSocket connection",
                                "/api/status - Server status"
                            )
                        ))
                    }

                    // Server status
                    get("/api/status") {
                        call.respond(mapOf(
                            "running" to _isRunning.value,
                            "port" to port,
                            "connectedClients" to synchronized(activeSessions) { activeSessions.size },
                            "totalMessages" to _messageCount.value
                        ))
                    }

                    // WebSocket endpoint
                    webSocket("/ws") {
                        val sessionId = this.hashCode().toString(16)
                        Log.d(TAG, "WebSocket connected: $sessionId")

                        synchronized(activeSessions) {
                            activeSessions.add(this)
                        }
                        _connectedClients.value = synchronized(activeSessions) { activeSessions.size }

                        try {
                            // 연결 환영 메시지
                            send(Frame.Text("""{"type":"welcome","sessionId":"$sessionId"}"""))

                            // 메시지 수신 루프
                            for (frame in incoming) {
                                if (frame is Frame.Text) {
                                    val text = frame.readText()
                                    _messageCount.value++

                                    Log.d(TAG, "Received: ${text.take(100)}...")

                                    // Echo response (간단한 예시)
                                    val response = """{"type":"echo","data":${gson.toJson(text)},"timestamp":${System.currentTimeMillis()}}"""
                                    send(Frame.Text(response))
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "WebSocket error: $sessionId", e)
                        } finally {
                            synchronized(activeSessions) {
                                activeSessions.remove(this)
                            }
                            _connectedClients.value = synchronized(activeSessions) { activeSessions.size }
                            Log.d(TAG, "WebSocket disconnected: $sessionId")
                        }
                    }

                    // POST message endpoint
                    post("/api/message") {
                        val body = call.receiveText()
                        _messageCount.value++

                        Log.d(TAG, "POST message: ${body.take(100)}...")

                        call.respond(mapOf(
                            "status" to "received",
                            "timestamp" to System.currentTimeMillis()
                        ))
                    }
                }
            }.start(wait = false)

            _isRunning.value = true
            Log.i(TAG, "Gateway server started successfully on port $port")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start server", e)
            _isRunning.value = false
            false
        }
    }

    /**
     * 서버 중지
     */
    fun stop() {
        if (!_isRunning.value) {
            return
        }

        Log.i(TAG, "Stopping Gateway server...")

        // 서버 종료 (WebSocket 연결도 자동으로 종료됨)
        server?.stop(1000, 2000)
        server = null

        synchronized(activeSessions) {
            activeSessions.clear()
        }
        _connectedClients.value = 0
        _isRunning.value = false

        Log.i(TAG, "Gateway server stopped")
    }

    /**
     * 서버 상태 정보
     */
    fun getStatus(): ServerStatus {
        return ServerStatus(
            isRunning = _isRunning.value,
            port = port,
            connectedClients = synchronized(activeSessions) { activeSessions.size },
            totalMessages = _messageCount.value
        )
    }

    data class ServerStatus(
        val isRunning: Boolean,
        val port: Int,
        val connectedClients: Int,
        val totalMessages: Long
    )
}
