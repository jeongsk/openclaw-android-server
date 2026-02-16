# OpenClaw Android 설치 스크립트
# Termux 환경에서 실행됩니다

#!/data/data/ai.openclaw.android.server/files/usr/bin/sh

# 환경 변수 설정
export PREFIX=/data/data/ai.openclaw.android.server/files/usr
export HOME=/data/data/ai.openclaw.android.server/files
export PATH=$PREFIX/bin:$PATH

echo "=== OpenClaw Android Installer ==="
echo ""

# 1. 패키지 업데이트
echo "[1/4] 패키지 업데이트 중..."
pkg update -y
pkg upgrade -y

# 2. Node.js 설치
echo "[2/4] Node.js 설치 중..."
pkg install nodejs -y

# Node.js 버전 확인
echo "Node.js 버전:"
node --version

# 3. OpenClaw 설치
echo "[3/4] OpenClaw 설치 중..."
npm install -g openclaw@latest

# OpenClaw 버전 확인
echo "OpenClaw 버전:"
openclaw --version

# 4. 초기 설정
echo "[4/4] 초기 설정 중..."
openclaw onboard --skip-channels

echo ""
echo "=== 설치 완료 ==="
echo ""
echo "이제 앱에서 '시작' 버튼을 눌러 Gateway를 실행하세요."
