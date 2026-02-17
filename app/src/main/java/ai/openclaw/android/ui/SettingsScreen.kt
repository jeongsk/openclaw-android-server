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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import ai.openclaw.android.data.SettingsPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settings = remember { SettingsPreferences(context) }

    // DataStore에서 설정 읽기
    val autoStartEnabled by settings.autoStartEnabled.collectAsStateWithLifecycle(initialValue = false)
    val gatewayPort by settings.gatewayPort.collectAsStateWithLifecycle(initialValue = SettingsPreferences.DEFAULT_PORT)
    val anthropicKey by settings.anthropicApiKey.collectAsStateWithLifecycle(initialValue = "")
    val openaiKey by settings.openaiApiKey.collectAsStateWithLifecycle(initialValue = "")
    val telegramToken by settings.telegramBotToken.collectAsStateWithLifecycle(initialValue = "")

    var showAdvanced by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // 임시 입력 상태
    var portInput by remember { mutableStateOf(gatewayPort.toString()) }
    var anthropicKeyInput by remember { mutableStateOf(anthropicKey) }
    var openaiKeyInput by remember { mutableStateOf(openaiKey) }
    var telegramTokenInput by remember { mutableStateOf(telegramToken) }
    
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
                    onCheckedChange = {
                        // 비동기로 저장
                        coroutineScope.launch {
                            settings.setAutoStart(it)
                        }
                    }
                )

                SettingsInput(
                    title = "Gateway 포트",
                    value = portInput,
                    onValueChange = {
                        portInput = it
                        // 유효한 포트 번호면 저장
                        it.toIntOrNull()?.let { port ->
                            if (port in 1024..65535) {
                                coroutineScope.launch {
                                    settings.setGatewayPort(port)
                                }
                            }
                        }
                    },
                    placeholder = "18789"
                )
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // API 키 설정
            SettingsSection(title = "API 키") {
                SettingsSecureInput(
                    title = "Anthropic API Key",
                    value = anthropicKeyInput,
                    onValueChange = { anthropicKeyInput = it },
                    placeholder = "sk-ant-...",
                    onSave = {
                        coroutineScope.launch {
                            settings.setAnthropicApiKey(anthropicKeyInput)
                        }
                    }
                )

                SettingsSecureInput(
                    title = "OpenAI API Key",
                    value = openaiKeyInput,
                    onValueChange = { openaiKeyInput = it },
                    placeholder = "sk-...",
                    onSave = {
                        coroutineScope.launch {
                            settings.setOpenaiApiKey(openaiKeyInput)
                        }
                    }
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // 채널 설정
            SettingsSection(title = "채널") {
                SettingsSecureInput(
                    title = "Telegram Bot Token",
                    value = telegramTokenInput,
                    onValueChange = { telegramTokenInput = it },
                    placeholder = "123456:ABC-DEF...",
                    onSave = {
                        coroutineScope.launch {
                            settings.setTelegramBotToken(telegramTokenInput)
                        }
                    }
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
    placeholder: String = "",
    onSave: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    var hasFocus by remember { mutableStateOf(false) }

    // 포커스 해제 시 저장
    LaunchedEffect(hasFocus) {
        if (!hasFocus) {
            onSave()
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState -> hasFocus = focusState.hasFocus },
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
