# OpenClaw Android Server PRD

## 1. 개요 (Overview)

### 1.1 프로젝트 목적
OpenClaw Android Server는 Android 기기에서 실행되는 OpenClaw Gateway 서버를 제공하는 앱입니다. 사용자는 Android 기기 하나만 있으면 OpenClaw 시스템에 연결할 수 있습니다.

### 1.2 핵심 가치 제안
- **No Installation Needed**: 별도의 Node.js 설치나 Termux 설정 없이 바로 실행
- **Pure Kotlin**: Android 네이티브 Ktor 서버로 안정적이고 빠름
- **Always Available**: Foreground Service로 백그라운드에서 안정적으로 실행
- **Zero Config**: 기본 설정으로 즉시 사용 가능

## 2. 기술 스택 (Technical Stack)

### 2.1 핵심 기술
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Server**: Ktor (Netty Engine)
  - HTTP/WebSocket 서버
  - 순수 Kotlin 구현 (Node.js 불필요)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Async**: Coroutines + Flow
- **Data Persistence**: DataStore (Preferences)
- **Service**: Foreground Service (백그라운드 실행)

### 2.2 주요 라이브러리
```kotlin
// Ktor Server
implementation("io.ktor:ktor-server-core:2.3.7")
implementation("io.ktor:ktor-server-netty:2.3.7")
implementation("io.ktor:ktor-server-websockets:2.3.7")
implementation("io.ktor:ktor-server-cors:2.3.7")
implementation("io.ktor:ktor-server-content-negotiation:2.3.7")

// Android
implementation("androidx.datastore:datastore-preferences:1.0.0")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
```

### 2.3 기술적 제약사항
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **Battery Optimization**: 사용자에게 비활성화 요청 필요
- **Network**: 로컬 Wi-Fi 네트워크에서만 동작
- **Port**: 기본 18789 포트 사용 (사용자 설정 가능)

## 3. 기능 요구사항 (Functional Requirements)

### 3.1 핵심 기능

#### FR-1: Gateway 서버 시작/중지
- **설명**: 사용자가 버튼으로 Gateway 서버 시작/중지
- **동작**:
  - 시작: Foreground Service 시작 → Ktor 서버 실행 → WebSocket 엔드포인트 제공
  - 중지: 서버 중지 → Service 종료
- **상태**: 실행 중, 중지됨, 오류

#### FR-2: WebSocket 통신
- **엔드포인트**: `/ws`
- **기능**:
  - WebSocket 연결 수락
  - 양방향 메시지 전달
  - 연결된 클라이언트 수 추적
- **메시지 형식**: JSON (OpenClaw 프로토콜)

#### FR-3: HTTP API
- **엔드포인트**:
  - `GET /health`: 서버 상태 확인
  - `GET /`: 서비스 정보
  - `GET /api/status`: 상태 정보 (연결된 클라이언트 수, 메시지 수)
  - `POST /api/message`: 메시지 브로드캐스트

#### FR-4: 설정 관리
- **API Keys**:
  - Anthropic API Key
  - OpenAI API Key
- **Channels**:
  - Telegram Bot Token
  - Discord Bot Token
- **General**:
  - Gateway Port (기본값: 18789)
  - Auto-start on Boot
- **저장**: DataStore (암호화되지 않음, 보안 주의)

#### FR-5: 부팅 시 자동 시작
- **설정**: 사용자 옵트인
- **동작**: BOOT_COMPLETED 브로드캐스트 수신 → 설정 확인 → 서비스 시작
- **제한**: Android 8.0+에서는 앱이 실행된 적 있어야 함

#### FR-6: 알림 (Notification)
- **Foreground Service 필수**: 영구 알림 표시
- **내용**: 서버 상태, URL (ws://IP:PORT)
- **클릭**: MainActivity로 이동

### 3.2 UI 요구사항

#### UI-1: 메인 화면
- 서버 상태 표시 (실행 중/중지됨/오류)
- 시작/중지 버튼
- Gateway URL 표시
- 연결된 클라이언트 수
- 설정 버튼

#### UI-2: 설정 화면
- API Keys 섹션 (보안 입력)
- Channels 섹션
- 일반 설정 섹션
- 고급 설정 섹션 (로그 보기, 초기화)
- 정보 섹션 (버전, 문서 링크)

## 4. 비기능 요구사항 (Non-Functional Requirements)

### 4.1 성능
- 서버 시작 시간: 3초 이내
- WebSocket 지연: 100ms 이내
- 메모리 사용: 150MB 이하 (유휴 시)

### 4.2 신뢰성
- 크래시률: 0.1% 이하
- WebSocket 재연결: 자동 지원
- 서버 다시 시작: 오류 시 자동

### 4.3 보안
- API Key 저장: DataStore (암호화되지 않음)
- 네트워크: 로컬 전용
- CORS: 모든 출처 허용 (개발용)

### 4.4 배터리
- Foreground Service: 배터리 최적화 예외 필요
- 유휴 시 배터리 소모: 최소화

## 5. 아키텍처 (Architecture)

### 5.1 구성도
```
┌─────────────────────────────────────────┐
│         MainActivity (UI)                │
│  ┌───────────────────────────────────┐  │
│  │   GatewayViewModel                │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│   OpenClawGatewayService (Foreground)   │
│  ┌───────────────────────────────────┐  │
│  │   GatewayServer (Ktor)            │  │
│  │   - HTTP Endpoints                │  │
│  │   - WebSocket /ws                 │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
                 │
                 ▼
         DataStore (Settings)
```

### 5.2 핵심 컴포넌트

#### GatewayServer.kt
- Ktor Netty 서버 캡슐화
- 시작/중지 메서드
- WebSocket 연결 관리
- 연결된 클라이언트 추적

#### OpenClawGatewayService.kt
- Foreground Service
- 서버 라이프사이클 관리
- 알림 관리
- 상태 Flow 제공

#### GatewayViewModel.kt
- UI와 Service 간 중개
- StateFlow로 상태 노출
- 시작/중지 메서드

#### SettingsManager.kt
- DataStore 래퍼
- 설정 Flow 제공
- 비동기 저장 메서드

## 6. 구현 단계 (Implementation Phases)

### Phase 1: 기본 서버 (완료)
- [x] GatewayServer 구현
- [x] WebSocket 엔드포인트
- [x] HTTP API 엔드포인트
- [x] Foreground Service
- [x] 기본 UI

### Phase 2: 설정 관리 (완료)
- [x] DataStore 설정 저장소
- [x] API Key 입력 UI
- [x] Port 설정
- [x] Auto-start 설정

### Phase 3: 고급 기능 (진행 중)
- [ ] 배터리 최적화 예외 요청
- [ ] 로그 화면
- [ ] 설정 초기화
- [ ] 연결된 클라이언트 관리

### Phase 4: 최적화
- [ ] WebSocket 재연결 로직
- [ ] 에러 복구 메커니즘
- [ ] 배터리 최적화
- [ ] 메모리 누수 방지

## 7. 인수 기준 (Acceptance Criteria)

### AC-1: 서버 시작
- [ ] 시작 버튼 클릭 → 서버 상태 "실행 중"으로 변경
- [ ] WebSocket 엔드포인트 `ws://IP:PORT/ws` 접속 가능
- [ ] 연결된 클라이언트 수가 정확히 표시됨
- [ ] 영구 알림이 표시됨

### AC-2: 서버 중지
- [ ] 중지 버튼 클릭 → 서버 상태 "중지됨"으로 변경
- [ ] WebSocket 연결이 거부됨
- [ ] 알림이 제거됨

### AC-3: 설정 저장
- [ ] API Key 입력 → 포커스 해제 시 DataStore에 저장
- [ ] 앱 재시작 후 설정이 유지됨
- [ ] Port 변경 시 서버가 해당 포트에서 실행됨

### AC-4: 부팅 시 자동 시작
- [ ] Auto-start 활성화 → 기기 재부팅 시 서버 자동 시작
- [ ] Auto-start 비활성화 → 기기 재부팅 시 서버 시작 안 함
- [ ] BOOT_COMPLETED 브로드캐스트 수신 가능

### AC-5: 오류 처리
- [ ] 포트 충돌 시 사용자에게 메시지 표시
- [ ] 네트워크 없음 시 "오류" 상태 표시
- [ ] 서버 시작 실패 시 알림에 오류 메시지 표시

## 8. 릴리스 계획 (Release Plan)

### v1.0.0 (MVP)
- 기본 서버 기능
- 설정 저장소
- 부팅 시 자동 시작
- 배터리 최적화 예외 요청

### v1.1.0 (향후)
- 로그 화면
- 연결된 클라이언트 관리
- 메시지 브로드캐스트
- 설정 가져오기/내보내기

### v2.0.0 (미래)
- mDNS (Bonjour) 지원
- 자동 포트 할당
- 보안 연결 (WSS)
- 원격 제어

## 9. 알려진 제한사항 (Known Limitations)

1. **로컬 전용**: 같은 Wi-Fi 네트워크에서만 접속 가능
2. **보안**: API Key가 암호화되지 않음
3. **배터리**: Foreground Service로 배터리 소모 있음
4. **Android 제한**: 백그라운드 제약으로 인해 연결이 끊길 수 있음

## 10. 개발 참고사항 (Development Notes)

### 10.1 디버깅
- adb logcat | grep OpenClawGatewayService
- adb logcat | grep GatewayServer
- WebSocket 테스트: wscat -c ws://IP:PORT/ws

### 10.2 테스트
- 단위 테스트: GatewayServer 로직
- 통합 테스트: Service + Server 연동
- UI 테스트: Compose UI 상태 변경

### 10.3 문제 해결
- 포트 충돌: Port 설정에서 변경
- SELinux: Ktor는 순수 Kotlin이라 문제 없음
- 배터리 최적화: 설정에서 예외 처리 필요

---

**문서 버전**: 2.0.0 (Ktor 아키텍처)
**마지막 업데이트**: 2026-02-17
**상태**: Ralph Loop 진행 중
