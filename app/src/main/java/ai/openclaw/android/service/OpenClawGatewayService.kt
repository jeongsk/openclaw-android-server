package ai.openclaw.android.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import ai.openclaw.android.OpenClawApplication
import ai.openclaw.android.MainActivity
import ai.openclaw.android.R
import ai.openclaw.android.installer.TermuxInstaller
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * OpenClaw Gateway를 실행하는 Foreground Service
 * 
 * 이 서비스는:
 * 1. Termux 환경을 초기화
 * 2. Node.js와 OpenClaw를 설치 (필요한 경우)
 * 3. Gateway 프로세스를 시작하고 유지
 */
class OpenClawGatewayService : Service() {
    
    companion object {
        const val TAG = "OpenClawGatewayService"
        const val NOTIFICATION_ID = 1001
        
        // Actions
        const val ACTION_START = "ai.openclaw.android.action.START"
        const val ACTION_STOP = "ai.openclaw.android.action.STOP"
        const val ACTION_RESTART = "ai.openclaw.android.action.RESTART"
        
        // 상태 Flow
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning
        
        private val _statusMessage = MutableStateFlow("중지됨")
        val statusMessage: StateFlow<String> = _statusMessage
        
        private val _statusDetail = MutableStateFlow("")
        val statusDetail: StateFlow<String> = _statusDetail
        
        private val _gatewayUrl = MutableStateFlow("")
        val gatewayUrl: StateFlow<String> = _gatewayUrl
        
        private val _installProgress = MutableStateFlow(0)
        val installProgress: StateFlow<Int> = _installProgress
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var gatewayProcess: Process? = null
    private lateinit var installer: TermuxInstaller
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        installer = TermuxInstaller(this)
        startForeground(NOTIFICATION_ID, createNotification("OpenClaw Gateway", "초기화 중..."))
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_START -> startGateway()
            ACTION_STOP -> stopGateway()
            ACTION_RESTART -> {
                stopGateway()
                serviceScope.launch {
                    delay(1000)
                    startGateway()
                }
            }
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        stopGatewayInternal()
        serviceScope.cancel()
        super.onDestroy()
    }
    
    private fun startGateway() {
        if (_isRunning.value) {
            Log.w(TAG, "Gateway already running")
            return
        }
        
        serviceScope.launch {
            try {
                _statusMessage.value = "초기화 중..."
                _statusDetail.value = "설치 상태 확인 중..."
                _installProgress.value = 0
                updateNotification("설치 확인 중...")
                
                // 1. 전체 설치 확인
                if (!installer.isInstalled()) {
                    Log.i(TAG, "Installation required, starting...")
                    
                    // Bootstrap 설치
                    _statusDetail.value = "Termux 환경 설치 중..."
                    _installProgress.value = 10
                    updateNotification("Termux 환경 설치 중...")
                    
                    if (!installer.installBootstrap()) {
                        throw Exception("Bootstrap 설치 실패")
                    }
                    _installProgress.value = 30
                    
                    // Node.js 설치
                    _statusDetail.value = "Node.js 설치 중..."
                    updateNotification("Node.js 설치 중... (시간이 걸릴 수 있습니다)")
                    
                    if (!installer.installNodeJs()) {
                        throw Exception("Node.js 설치 실패")
                    }
                    _installProgress.value = 60
                    
                    // OpenClaw 설치
                    _statusDetail.value = "OpenClaw 설치 중..."
                    updateNotification("OpenClaw 설치 중...")
                    
                    if (!installer.installOpenClaw()) {
                        throw Exception("OpenClaw 설치 실패")
                    }
                    _installProgress.value = 90
                }
                
                // 2. Gateway 시작
                _statusDetail.value = "Gateway 시작 중..."
                _installProgress.value = 95
                updateNotification("Gateway 시작 중...")
                
                gatewayProcess = installer.startGateway()
                
                // 3. 상태 업데이트
                val ip = installer.getLocalIpAddress()
                val port = 18789
                _gatewayUrl.value = "ws://$ip:$port"
                _isRunning.value = true
                _statusMessage.value = "실행 중"
                _statusDetail.value = "Node ${installer.getNodeVersion()}"
                _installProgress.value = 100
                
                updateNotification("실행 중", "$ip:$port")
                
                Log.i(TAG, "Gateway started at ${_gatewayUrl.value}")
                
                // 프로세스 종료 감시
                watchProcess()
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start gateway", e)
                _isRunning.value = false
                _statusMessage.value = "오류"
                _statusDetail.value = e.message ?: "알 수 없는 오류"
                _installProgress.value = 0
                updateNotification("오류", e.message ?: "알 수 없는 오류")
            }
        }
    }
    
    private fun stopGateway() {
        Log.d(TAG, "Stopping gateway (external request)")
        stopGatewayInternal()
    }
    
    private fun stopGatewayInternal() {
        gatewayProcess?.destroy()
        gatewayProcess = null
        _isRunning.value = false
        _statusMessage.value = "중지됨"
        _statusDetail.value = ""
        _gatewayUrl.value = ""
        _installProgress.value = 0
        updateNotification("중지됨", "")
    }
    
    private fun watchProcess() {
        serviceScope.launch {
            val process = gatewayProcess ?: return@launch
            val exitCode = process.waitFor()
            
            if (_isRunning.value) {
                Log.w(TAG, "Gateway process exited unexpectedly with code $exitCode")
                _isRunning.value = false
                _statusMessage.value = "중단됨"
                _statusDetail.value = "종료 코드: $exitCode"
                updateNotification("중단됨", "종료 코드: $exitCode")
            }
        }
    }
    
    private fun createNotification(title: String, message: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, OpenClawApplication.CHANNEL_ID_GATEWAY)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
    
    private fun updateNotification(title: String, message: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(title, message))
    }
    
    private suspend fun delay(millis: Long) {
        kotlinx.coroutines.delay(millis)
    }
}
