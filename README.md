# 🦞 OpenClaw Android Server

> 오래된 안드로이드 폰을 OpenClaw Gateway 서버로 변환하는 APK

[![Android CI](https://github.com/jeongsk/openclaw-android-server/actions/workflows/android.yml/badge.svg)](https://github.com/jeongsk/openclaw-android-server/actions/workflows/android.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 📱 개요

이 프로젝트는 안드로이드 폰에서 **한 번의 터치**로 OpenClaw Gateway를 실행할 수 있게 해주는 앱입니다.

### 왜 이 앱이 필요한가요?

- 📱 **오래된 폰 활용**: 구형 안드로이드 폰을 AI 어시스턴트 서버로 변환
- 🔌 **24/7 실행**: Foreground Service로 백그라운드에서도 안정적 실행
- 🌐 **원격 접속**: 같은 WiFi의 다른 기기에서 접속 가능
- 🔋 **저전력**: 노트북이나 데스크톱 대비 적은 전력 소모

## ✨ 기능

- ✅ **원터치 설치**: 시작 버튼만 누르면 자동 설치
- ✅ **Termux 환경**: Node.js 22 + OpenClaw 자동 설치
- ✅ **Foreground Service**: 백그라운드에서도 안정적 실행
- ✅ **실시간 상태**: 설치 진행률, 실행 상태 표시
- ✅ **연결 정보**: IP 주소 + 포트 쉽게 확인
- ✅ **자동 시작**: 부팅 시 자동으로 Gateway 실행
- ✅ **설정 저장**: API 키, 채널 설정 안전하게 저장

## 🔧 기술 스택

| 구성 요소 | 기술 |
|---------|------|
| Runtime | Termux Embedded (Node.js 22) |
| UI | Jetpack Compose |
| Architecture | MVVM + Foreground Service |
| 설정 저장 | DataStore |
| 백그라운드 | Foreground Service + WorkManager |

## 📖 아키텍처

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

## 🚀 빠른 시작

### 요구사항

- 안드로이드 기기 (ARM64, Android 7.0+)
- 최소 500MB 저장 공간
- WiFi 연결

### 설치

1. [Releases](https://github.com/jeongsk/openclaw-android-server/releases)에서 APK 다운로드
2. APK 설치
3. 권한 허용 (알림, 저장소)
4. "시작" 버튼 클릭
5. 설치 완료까지 대기 (첫 실행 시 5-10분)

### 연결

```
# 같은 WiFi 네트워크에서
ws://192.168.1.xxx:18789

# OpenClaw CLI로 연결
openclaw gateway connect ws://192.168.1.xxx:18789
```

## 🛠️ 개발

### 빌드 요구사항

- Android Studio Koala (2024.1.1) 이상
- JDK 17
- Android SDK 34

### 빌드 방법

```bash
# 클론
git clone https://github.com/jeongsk/openclaw-android-server.git
cd openclaw-android-server

# Debug APK 빌드
./gradlew assembleDebug

# Release APK 빌드
./gradlew assembleRelease
```

자세한 내용은 [DEVELOPMENT.md](DEVELOPMENT.md) 참조

## 📱 스크린샷

| 메인 화면 | 설정 |
|---------|------|
| (준비 중) | (준비 중) |

## 🤝 기여

Pull Request 환영합니다! 

[CONTRIBUTING.md](CONTRIBUTING.md)를 참조해주세요.

## 📄 라이선스

MIT License - [LICENSE](LICENSE)

## 🦞 OpenClaw

이 프로젝트는 [OpenClaw](https://github.com/openclaw/openclaw)의 안드로이드 런타임입니다.

- Website: https://openclaw.ai
- Docs: https://docs.openclaw.ai
- Discord: https://discord.gg/clawd

---

Made with 🦞 by OpenClaw Community
