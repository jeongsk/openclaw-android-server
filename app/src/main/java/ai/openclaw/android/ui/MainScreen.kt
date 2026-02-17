package ai.openclaw.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.openclaw.android.viewmodel.GatewayViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GatewayViewModel = viewModel(),
    onNavigateToSettings: () -> Unit = {}
) {
    val isRunning by viewModel.isRunning.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val statusDetail by viewModel.statusDetail.collectAsState()
    val gatewayUrl by viewModel.gatewayUrl.collectAsState()
    val installProgress by viewModel.installProgress.collectAsState()
    val isStarting by viewModel.isStarting.collectAsState()
    
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("🦞 OpenClaw Server") 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "설정")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 상태 카드
            StatusCard(
                isRunning = isRunning,
                statusMessage = statusMessage,
                statusDetail = statusDetail,
                gatewayUrl = gatewayUrl,
                installProgress = installProgress
            )
            
            // 컨트롤 버튼
            ControlButtons(
                isRunning = isRunning,
                isStarting = isStarting,
                installProgress = installProgress,
                onStart = { viewModel.startGateway() },
                onStop = { viewModel.stopGateway() }
            )
            
            // 연결 정보
            if (gatewayUrl.isNotEmpty()) {
                ConnectionInfo(
                    gatewayUrl = gatewayUrl,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(gatewayUrl))
                    }
                )
            }
            
            // 안내 카드
            InfoCard()
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatusCard(
    isRunning: Boolean,
    statusMessage: String,
    statusDetail: String,
    gatewayUrl: String,
    installProgress: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isRunning -> MaterialTheme.colorScheme.primaryContainer
                installProgress > 0 -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상태 아이콘
            Box(contentAlignment = Alignment.Center) {
                if (installProgress in 1..99) {
                    CircularProgressIndicator(
                        progress = installProgress / 100f,
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "$installProgress%",
                        style = MaterialTheme.typography.labelMedium
                    )
                } else {
                    Icon(
                        imageVector = when {
                            isRunning -> Icons.Default.CheckCircle
                            statusMessage == "오류" -> Icons.Default.Error
                            else -> Icons.Default.StopCircle
                        },
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = when {
                            isRunning -> MaterialTheme.colorScheme.primary
                            statusMessage == "오류" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 상태 텍스트
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            
            if (statusDetail.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusDetail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (gatewayUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = gatewayUrl,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ControlButtons(
    isRunning: Boolean,
    isStarting: Boolean,
    installProgress: Int,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val isLoading = isStarting || installProgress in 1..99
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 시작 버튼
        Button(
            onClick = onStart,
            enabled = !isRunning && !isLoading,
            modifier = Modifier.weight(1f).height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("설치 중...")
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("시작")
            }
        }
        
        // 중지 버튼
        Button(
            onClick = onStop,
            enabled = isRunning,
            modifier = Modifier.weight(1f).height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(Icons.Default.Stop, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("중지")
        }
    }
}

@Composable
fun ConnectionInfo(
    gatewayUrl: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "연결 정보",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = gatewayUrl,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Default.ContentCopy, "복사")
                    }
                },
                supportingText = { Text("같은 WiFi 네트워크의 다른 기기에서 접속") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // QR 코드 안내
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.QrCode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "QR 코드로 스캔하여 접속",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun InfoCard() {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "사용 방법",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        "펼치기/접기"
                    )
                }
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                val steps = listOf(
                    "시작" to "'시작' 버튼을 누르면 자동으로 설치 및 실행됩니다",
                    "연결" to "같은 WiFi의 다른 기기에서 연결 정보의 URL로 접속",
                    "설정" to "API 키, 채널 설정은 설정 메뉴에서",
                    "자동시작" to "부팅 시 자동 시작 설정 가능"
                )
                
                steps.forEach { (title, desc) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
