package ai.openclaw.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ai.openclaw.android.service.OpenClawGatewayService
import ai.openclaw.android.util.SettingsManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * 부팅 완료 시 자동 시작
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(TAG, "Boot completed, checking auto-start preference")
            
            // 설정에 따라 자동 시작 여부 결정
            val settingsManager = SettingsManager(context)
            
            runBlocking {
                val autoStart = settingsManager.autoStart.first()
                Log.i(TAG, "Auto-start enabled: $autoStart")
                
                if (autoStart) {
                    startGateway(context)
                }
            }
        }
    }
    
    private fun startGateway(context: Context) {
        Log.i(TAG, "Starting OpenClaw Gateway...")
        val serviceIntent = Intent(context, OpenClawGatewayService::class.java).apply {
            action = OpenClawGatewayService.ACTION_START
        }
        context.startForegroundService(serviceIntent)
    }
}
