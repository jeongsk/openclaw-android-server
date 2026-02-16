package ai.openclaw.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var autoStartEnabled by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(false) }
    
    // API 키 상태
    var anthropicKey by remember { mutableStateOf("") }
    var openaiKey by remember { mutableStateOf("") }
    var telegramToken by remember { mutableStateOf("") }
    var gatewayPort by remember { mutableStateOf("18789") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // 일반 설정
            SettingsSection(title = "일반") {
                SettingsToggle(
                    title = "부팅 시 자동 시작",
                    description = "기기 시작 시 자동으로 Gateway 실행",
                    checked = autoStartEnabled,
                    onCheckedChange = { autoStartEnabled = it }
                )
                
                SettingsInput(
                    title = "Gateway 포트",
                    value = gatewayPort,
                    onValueChange = { gatewayPort = it },
                    placeholder = "18789"
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // API 키 설정
            SettingsSection(title = "API 키") {
                SettingsSecureInput(
                    title = "Anthropic API Key",
                    value = anthropicKey,
                    onValueChange = { anthropicKey = it },
                    placeholder = "sk-ant-..."
                )
                
                SettingsSecureInput(
                    title = "OpenAI API Key",
                    value = openaiKey,
                    onValueChange = { openaiKey = it },
                    placeholder = "sk-..."
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 채널 설정
            SettingsSection(title = "채널") {
                SettingsSecureInput(
                    title = "Telegram Bot Token",
                    value = telegramToken,
                    onValueChange = { telegramToken = it },
                    placeholder = "123456:ABC-DEF..."
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 고급 설정
            SettingsSection(title = "고급") {
                SettingsToggle(
                    title = "고급 옵션 표시",
                    description = "개발자용 옵션 표시",
                    checked = showAdvanced,
                    onCheckedChange = { showAdvanced = it }
                )
                
                if (showAdvanced) {
                    SettingsItem(
                        title = "로그 보기",
                        description = "Gateway 로그 확인",
                        onClick = { /* 로그 화면 */ }
                    )
                    
                    SettingsItem(
                        title = "설정 초기화",
                        description = "모든 설정을 기본값으로 복원",
                        onClick = { /* 초기화 */ },
                        isDestructive = true
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // 정보
            SettingsSection(title = "정보") {
                SettingsItem(
                    title = "버전",
                    description = "1.0.0",
                    onClick = { }
                )
                
                SettingsItem(
                    title = "OpenClaw 문서",
                    description = "docs.openclaw.ai",
                    onClick = { /* 웹으로 이동 */ }
                )
                
                SettingsItem(
                    title = "GitHub",
                    description = "github.com/openclaw/openclaw",
                    onClick = { /* 웹으로 이동 */ }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsToggle(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsInput(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun SettingsSecureInput(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = ""
) {
    var visible by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (visible) 
                androidx.compose.ui.text.input.VisualTransformation.None 
            else 
                androidx.compose.ui.text.input.PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        "표시/숨기기"
                    )
                }
            }
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) MaterialTheme.colorScheme.error 
                       else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
