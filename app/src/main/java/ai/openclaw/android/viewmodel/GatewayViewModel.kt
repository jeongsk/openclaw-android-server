package ai.openclaw.android.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ai.openclaw.android.service.OpenClawGatewayService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GatewayViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application
    
    // Service 상태 직접 참조
    val isRunning: StateFlow<Boolean> = OpenClawGatewayService.isRunning
        .stateIn(viewModelScope, SharingStarted.Lazily, false)
    
    val statusMessage: StateFlow<String> = OpenClawGatewayService.statusMessage
        .stateIn(viewModelScope, SharingStarted.Lazily, "중지됨")
    
    val statusDetail: StateFlow<String> = OpenClawGatewayService.statusDetail
        .stateIn(viewModelScope, SharingStarted.Lazily, "")
    
    val gatewayUrl: StateFlow<String> = OpenClawGatewayService.gatewayUrl
        .stateIn(viewModelScope, SharingStarted.Lazily, "")
    
    val installProgress: StateFlow<Int> = OpenClawGatewayService.installProgress
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)
    
    // UI 상태
    private val _isStarting = MutableStateFlow(false)
    val isStarting: StateFlow<Boolean> = _isStarting.asStateFlow()
    
    fun startGateway() {
        if (_isStarting.value) return
        
        _isStarting.value = true
        val intent = Intent(context, OpenClawGatewayService::class.java).apply {
            action = OpenClawGatewayService.ACTION_START
        }
        context.startForegroundService(intent)
        
        // 설치 완료 후 starting 상태 해제
        viewModelScope.launch {
            // 진행률이 100이 되거나 실행 중이 되면 해제
            while (true) {
                if (isRunning.value || statusMessage.value == "오류") {
                    _isStarting.value = false
                    break
                }
                delay(500)
            }
        }
    }
    
    fun stopGateway() {
        val intent = Intent(context, OpenClawGatewayService::class.java).apply {
            action = OpenClawGatewayService.ACTION_STOP
        }
        context.startService(intent)
    }
    
    fun restartGateway() {
        val intent = Intent(context, OpenClawGatewayService::class.java).apply {
            action = OpenClawGatewayService.ACTION_RESTART
        }
        context.startService(intent)
    }
}
