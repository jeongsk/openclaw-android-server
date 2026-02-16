# 🦞 OpenClaw Android Server

> 오래된 안드로이드 폰을 OpenClaw Gateway 서버로 변환하는 APK

## 개요

이 프로젝트는 안드로이드 폰에서 OpenClaw를 **한 번의 터치**로 실행할 수 있게 해주는 앱입니다.

### 기술 스택
- **Runtime:** Termux Embedded (Node.js 22)
- **UI:** Jetpack Compose
- **Architecture:** MVVM + Foreground Service

## 아키텍처

```
┌─────────────────────────────────────────┐
│  OpenClaw Android App (Kotlin)          │
├─────────────────────────────────────────┤
│  UI Layer (Jetpack Compose)             │
│         ↓                               │
│  ViewModel + Foreground Service         │
│         ↓                               │
│  Termux Embedded Environment            │
│         ↓                               │
│  Node.js 22 + OpenClaw                  │
└─────────────────────────────────────────┘
```

## 기능

- ✅ 원터치 설치 및 실행
- ✅ Termux 환경 자동 설정
- ✅ Node.js 22 자동 설치
- ✅ OpenClaw 자동 설치
- ✅ Foreground Service (백그라운드 실행)
- ✅ 상태 표시 (설치 진행률, 실행 상태)
- ✅ 연결 정보 표시 (IP + 포트)
- ✅ 부팅 시 자동 시작

## 설치 방법

### 1. APK 다운로드

```bash
# 릴리즈 페이지에서 다운로드
# 또는 직접 빌드
```

### 2. 권한 허용

앱 설치 후 다음 권한을 허용해야 합니다:
- 알림 권한
- 저장소 권한 (일부 기기)

### 3. 시작

"시작" 버튼을 누르면 자동으로:
1. Termux 환경 설치
2. Node.js 22 설치
3. OpenClaw 설치
4. Gateway 실행

## 빌드

### 요구사항
- Android Studio Koala 이상
- JDK 17
- Android SDK 34

### 빌드 명령

```bash
# 프로젝트 클론
cd /home/ubuntu/.openclaw/workspace/projects/openclaw-android

# Debug APK 빌드
./gradlew assembleDebug

# Release APK 빌드
./gradlew assembleRelease
```

### APK 위치

```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release.apk
```

## Termux Bootstrap 준비

> ⚠️ 실제 빌드를 위해서는 Termux Bootstrap 파일이 필요합니다.

### Bootstrap 다운로드

```bash
# ARM64용 Bootstrap
wget https://github.com/termux/termux-packages/releases/download/bootstrap-v1.0.0/bootstrap-aarch64.zip

# app/src/main/assets/ 폴더에 복사
cp bootstrap-aarch64.zip app/src/main/assets/termux-bootstrap.zip
```

## 사용 방법

### Gateway 실행

1. 앱 실행
2. "시작" 버튼 클릭
3. 설치 완료까지 대기 (첫 실행 시 5-10분 소요)
4. "실행 중" 상태 확인
5. 표시된 URL로 다른 기기에서 접속

### 연결

```
# 같은 WiFi 네트워크에서
ws://192.168.1.xxx:18789

# OpenClaw CLI로 연결
openclaw gateway connect ws://192.168.1.xxx:18789
```

## 설정

### API 키 설정

설정 화면에서:
- Anthropic API Key
- OpenAI API Key
- Telegram Bot Token
- 기타 채널 설정

### 자동 시작

부팅 시 자동으로 Gateway를 시작하려면:
1. 설정 → 자동 시작 활성화
2. 배터리 최적화 제외 설정

## 문제 해결

### 설치 실패

```
1. 앱 데이터 삭제 후 재시도
2. 저장소 권한 확인
3. 인터넷 연결 확인
```

### Gateway 시작 실패

```
1. 로그 확인 (adb logcat | grep OpenClaw)
2. 포트 18789가 사용 중인지 확인
3. 메모리 부족 확인
```

### 연결 안 됨

```
1. 같은 WiFi 네트워크인지 확인
2. 방화벽 설정 확인
3. IP 주소 재확인
```

## 개발 일정

| 주차 | 작업 | 상태 |
|-----|------|------|
| 1주 | 프로젝트 구조, 기본 UI | ✅ 완료 |
| 2주 | Termux 임베딩, 설치 자동화 | 🔄 진행 중 |
| 3주 | Foreground Service, 상태 관리 | ⬜ 예정 |
| 4주 | 테스트, 최적화, 배포 | ⬜ 예정 |

## 기여

Pull Request 환영합니다!

## 라이선스

MIT

---

Made with 🦞 by OpenClaw Community
