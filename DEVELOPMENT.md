# OpenClaw Android 개발 가이드

## 빠른 시작

### 1. 사전 요구사항

- Android Studio Koala (2024.1.1) 이상
- JDK 17
- Android SDK 34
- 안드로이드 기기 (ARM64, API 24+)

### 2. 프로젝트 열기

```bash
# Android Studio에서
File → Open → openclaw-android 폴더 선택
```

### 3. Gradle Sync

프로젝트를 열면 자동으로 Gradle Sync가 실행됩니다.
실행되지 않으면: `File → Sync Project with Gradle Files`

### 4. 빌드

```bash
# Debug APK
./gradlew assembleDebug

# 또는 Android Studio에서
Build → Make Project (Ctrl+F9)
```

### 5. 설치 및 실행

```bash
# USB로 연결된 기기에 설치
./gradlew installDebug

# 또는 Android Studio에서 Run (Shift+F10)
```

## 프로젝트 구조

```
app/src/main/java/ai/openclaw/android/
├── MainActivity.kt           # 메인 액티비티
├── OpenClawApplication.kt    # Application 클래스
├── installer/
│   └── TermuxInstaller.kt    # Termux 설치 관리
├── service/
│   └── OpenClawGatewayService.kt  # Gateway 실행 서비스
├── viewmodel/
│   └── GatewayViewModel.kt   # UI 상태 관리
├── receiver/
│   └── BootReceiver.kt       # 부팅 시 자동 시작
└── ui/
    ├── MainScreen.kt         # 메인 화면
    ├── SettingsScreen.kt     # 설정 화면
    └── theme/                # 테마
```

## Termux Bootstrap

프로젝트에는 이미 Termux Bootstrap 파일이 포함되어 있습니다:
- `app/src/main/assets/termux-bootstrap.zip` (~29MB)

이 파일은 ARM64 안드로이드 기기를 위한 Termux 환경입니다.

## 주요 기능

### TermuxInstaller
- Bootstrap 압축 해제
- Node.js 22 설치
- OpenClaw 설치
- Gateway 프로세스 시작

### OpenClawGatewayService
- Foreground Service로 실행
- 설치 진행률 상태 제공
- 자동 재시작 지원

### UI
- 설치 진행률 표시
- 실행 상태 표시
- 연결 정보 (IP + 포트)
- API 키 설정

## 디버깅

### 로그 보기

```bash
# 전체 로그
adb logcat

# OpenClaw 관련만
adb logcat | grep -E "OpenClaw|Termux"

# 서비스만
adb logcat -s OpenClawGatewayService
```

### 일반적인 문제

**1. 설치 실패**
```
- 저장소 권한 확인
- 인터넷 연결 확인
- 충분한 저장 공간 확인 (최소 500MB)
```

**2. Gateway 시작 실패**
```
- 로그 확인: adb logcat -s OpenClaw
- 포트 18789 사용 중인지 확인
- 메모리 부족 확인
```

**3. 연결 안 됨**
```
- 같은 WiFi 네트워크인지 확인
- 방화벽 설정
- IP 주소 재확인
```

## 테스트

### 단위 테스트

```bash
./gradlew test
```

### UI 테스트

```bash
./gradlew connectedAndroidTest
```

## 릴리즈 빌드

### 1. 서명 키 생성

```bash
keytool -genkey -v -keystore openclaw-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias openclaw
```

### 2. keystore.properties 생성

```properties
storePassword=your_password
keyPassword=your_key_password
keyAlias=openclaw
storeFile=openclaw-release.jks
```

### 3. Release APK 빌드

```bash
./gradlew assembleRelease
```

### APK 위치

```
app/build/outputs/apk/release/app-release.apk
```

## 기여

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing`)
5. Open Pull Request

## 라이선스

MIT
