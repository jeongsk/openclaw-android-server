package ai.openclaw.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class OpenClawApplication : Application() {
    
    companion object {
        const val CHANNEL_ID_GATEWAY = "openclaw_gateway"
        const val CHANNEL_ID_STATUS = "openclaw_status"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val gatewayChannel = NotificationChannel(
                CHANNEL_ID_GATEWAY,
                "Gateway Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "OpenClaw Gateway 실행 상태"
            }
            
            val statusChannel = NotificationChannel(
                CHANNEL_ID_STATUS,
                "Status Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "OpenClaw 상태 알림"
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(listOf(gatewayChannel, statusChannel))
        }
    }
}
