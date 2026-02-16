package ai.openclaw.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import ai.openclaw.android.service.OpenClawGatewayService

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
            
            // TODO: 사용자 설정에 따라 자동 시작 여부 결정
            // val prefs = context.getSharedPreferences("openclaw_prefs", Context.MODE_PRIVATE)
            // if (prefs.getBoolean("auto_start", false)) {
            //     startGateway(context)
            // }
            
            // 우선은 자동 시작
            startGateway(context)
        }
    }
    
    private fun startGateway(context: Context) {
        val serviceIntent = Intent(context, OpenClawGatewayService::class.java).apply {
            action = OpenClawGatewayService.ACTION_START
        }
        context.startForegroundService(serviceIntent)
    }
}
