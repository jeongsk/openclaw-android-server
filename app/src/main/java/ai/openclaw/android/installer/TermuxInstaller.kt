package ai.openclaw.android.installer

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.zip.ZipInputStream

/**
 * Termux 환경 설치 및 관리
 * 
 * Termux를 앱에 임베딩하여 Node.js와 OpenClaw를 실행합니다.
 */
class TermuxInstaller(private val context: Context) {
    
    companion object {
        const val TAG = "TermuxInstaller"
        
        // Termux 경로
        const val TERMUX_PREFIX = "usr"
        const val TERMUX_HOME = "home"
        
        // Bootstrap 파일명
        const val BOOTSTRAP_ZIP = "termux-bootstrap.zip"
    }
    
    private val filesDir = context.filesDir.absolutePath
    val termuxPrefix = "$filesDir/$TERMUX_PREFIX"
    val termuxHome = "$filesDir/$TERMUX_HOME"
    val termuxBin = "$termuxPrefix/bin"
    
    /**
     * Termux 환경이 설치되어 있는지 확인
     */
    fun isTermuxInstalled(): Boolean {
        val prefixDir = File(termuxPrefix)
        return prefixDir.exists() && File(termuxBin, "sh").exists()
    }
    
    /**
     * Node.js가 설치되어 있는지 확인
     */
    fun isNodeInstalled(): Boolean {
        return File(termuxBin, "node").exists()
    }
    
    /**
     * OpenClaw가 설치되어 있는지 확인
     */
    fun isOpenClawInstalled(): Boolean {
        return File(termuxBin, "openclaw").exists()
    }
    
    /**
     * 전체 설치 상태 확인
     */
    fun isInstalled(): Boolean {
        return isTermuxInstalled() && isNodeInstalled()
    }
    
    /**
     * Termux Bootstrap 설치
     * assets/termux-bootstrap.zip에서 압축 해제
     */
    suspend fun installBootstrap(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Installing Termux bootstrap...")
            
            // 디렉토리 생성
            File(termuxPrefix).mkdirs()
            File(termuxHome).mkdirs()
            
            // Bootstrap 압축 해제
            val assetManager = context.assets
            val inputStream = assetManager.open(BOOTSTRAP_ZIP)
            extractZip(inputStream, File(filesDir))
            inputStream.close()
            
            // 실행 권한 설정
            File(termuxBin).listFiles()?.forEach { file ->
                file.setExecutable(true, false)
            }
            
            Log.i(TAG, "Bootstrap installed successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install bootstrap", e)
            false
        }
    }
    
    /**
     * Node.js 설치
     */
    suspend fun installNodeJs(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Installing Node.js...")
            
            val result = executeCommand(
                "pkg install nodejs -y",
                environment = mapOf(
                    "PREFIX" to termuxPrefix,
                    "HOME" to termuxHome,
                    "PATH" to "$termuxBin:/system/bin"
                )
            )
            
            if (result == 0) {
                Log.i(TAG, "Node.js installed: ${getNodeVersion()}")
                true
            } else {
                Log.e(TAG, "Node.js installation failed with code $result")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install Node.js", e)
            false
        }
    }
    
    /**
     * OpenClaw 설치
     */
    suspend fun installOpenClaw(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Installing OpenClaw...")
            
            val result = executeCommand(
                "npm install -g openclaw@latest",
                environment = mapOf(
                    "PREFIX" to termuxPrefix,
                    "HOME" to termuxHome,
                    "PATH" to "$termuxBin:/system/bin",
                    "NODE_PATH" to "$termuxPrefix/lib/node_modules"
                )
            )
            
            if (result == 0) {
                Log.i(TAG, "OpenClaw installed: ${getOpenClawVersion()}")
                true
            } else {
                Log.e(TAG, "OpenClaw installation failed with code $result")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install OpenClaw", e)
            false
        }
    }
    
    /**
     * 전체 설치 (Bootstrap → Node.js → OpenClaw)
     */
    suspend fun fullInstall(): Boolean {
        return try {
            // 1. Bootstrap
            if (!isTermuxInstalled()) {
                if (!installBootstrap()) return false
            }
            
            // 2. Node.js
            if (!isNodeInstalled()) {
                if (!installNodeJs()) return false
            }
            
            // 3. OpenClaw
            if (!isOpenClawInstalled()) {
                if (!installOpenClaw()) return false
            }
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Full installation failed", e)
            false
        }
    }
    
    /**
     * Gateway 시작
     */
    fun startGateway(port: Int = 18789): Process {
        Log.i(TAG, "Starting OpenClaw Gateway on port $port...")
        
        val processBuilder = ProcessBuilder(
            "$termuxBin/openclaw",
            "gateway",
            "--port", port.toString(),
            "--verbose"
        )
        
        processBuilder.directory(File(termuxHome))
        processBuilder.environment().apply {
            put("PREFIX", termuxPrefix)
            put("HOME", termuxHome)
            put("PATH", "$termuxBin:/system/bin")
            put("NODE_PATH", "$termuxPrefix/lib/node_modules")
        }
        
        processBuilder.redirectErrorStream(true)
        
        return processBuilder.start().also { process ->
            // 로그 스트림
            Thread {
                try {
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        Log.d("OpenClaw", line ?: "")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading process output", e)
                }
            }.start()
        }
    }
    
    /**
     * Node.js 버전 확인
     */
    fun getNodeVersion(): String {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("$termuxBin/node", "--version"),
                arrayOf("PATH=$termuxBin", "PREFIX=$termuxPrefix"),
                File(termuxHome)
            )
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val version = reader.readLine() ?: "unknown"
            reader.close()
            version
        } catch (e: Exception) {
            "error: ${e.message}"
        }
    }
    
    /**
     * OpenClaw 버전 확인
     */
    fun getOpenClawVersion(): String {
        return try {
            val process = Runtime.getRuntime().exec(
                arrayOf("$termuxBin/openclaw", "--version"),
                arrayOf("PATH=$termuxBin", "PREFIX=$termuxPrefix"),
                File(termuxHome)
            )
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val version = reader.readLine() ?: "unknown"
            reader.close()
            version
        } catch (e: Exception) {
            "error: ${e.message}"
        }
    }
    
    /**
     * 로컬 IP 주소 가져오기
     */
    fun getLocalIpAddress(): String {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.isSiteLocalAddress) {
                        return address.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get IP address", e)
        }
        return "127.0.0.1"
    }
    
    /**
     * ZIP 압축 해제
     */
    private fun extractZip(inputStream: InputStream, targetDir: File) {
        val zipInputStream = ZipInputStream(BufferedInputStream(inputStream))
        var entry = zipInputStream.nextEntry
        
        while (entry != null) {
            val file = File(targetDir, entry.name)
            
            if (entry.isDirectory) {
                file.mkdirs()
            } else {
                file.parentFile?.mkdirs()
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8192)
                    var len: Int
                    while (zipInputStream.read(buffer).also { len = it } > 0) {
                        output.write(buffer, 0, len)
                    }
                }
                // 실행 권한 설정 (Unix 권한 유지)
                if (entry.name.contains("bin/")) {
                    file.setExecutable(true, false)
                }
            }
            
            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }
        
        zipInputStream.close()
    }
    
    /**
     * 명령어 실행
     */
    private fun executeCommand(
        command: String,
        environment: Map<String, String> = emptyMap(),
        workingDir: File = File(termuxHome)
    ): Int {
        val scriptFile = File(workingDir, "temp_script.sh")
        scriptFile.writeText("#!/system/bin/sh\n$command")
        scriptFile.setExecutable(true)
        
        val processBuilder = ProcessBuilder("/system/bin/sh", scriptFile.absolutePath)
        processBuilder.directory(workingDir)
        processBuilder.environment().putAll(environment)
        
        val process = processBuilder.start()
        
        // 출력 읽기
        Thread {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                Log.d("Termux", line ?: "")
            }
        }.start()
        
        // 에러 읽기
        Thread {
            val reader = BufferedReader(InputStreamReader(process.errorStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                Log.e("Termux", line ?: "")
            }
        }.start()
        
        val exitCode = process.waitFor()
        scriptFile.delete()
        return exitCode
    }
}
