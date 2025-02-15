#!/bin/bash

set -e  # 에러 발생 시 즉시 종료

REPO_PATH="/home/ubuntu/app/src/main/resources"
SUBMODULE_PATH="PeepIt-ServerConfig"
LOG_FILE="/home/ubuntu/app/deploy.log"

echo "🔄 [INFO] 서브모듈 초기화 및 업데이트 시작..." | tee -a $LOG_FILE
cd $REPO_PATH || { echo "❌ [ERROR] 디렉토리 이동 실패: $REPO_PATH" | tee -a $LOG_FILE; exit 1; }

# 🔥 Git 저장소 확인
if [ ! -d "$REPO_PATH/.git" ]; then
    echo "❌ [ERROR] Git 저장소가 없습니다. $REPO_PATH 가 올바른지 확인하세요." | tee -a $LOG_FILE
    exit 1
fi

# SSH Key 설정
eval "$(ssh-agent -s)" | tee -a $LOG_FILE
ssh-add ~/.ssh/id_rsa 2>&1 | tee -a $LOG_FILE

# SSH Key가 제대로 추가되었는지 확인
if ! ssh-add -l | grep -q "id_rsa"; then
    echo "❌ [ERROR] SSH Key 추가 실패! ~/.ssh/id_rsa 파일을 확인하세요." | tee -a $LOG_FILE
    exit 1
fi

# 🔄 서브모듈 정보 동기화
echo "🔄 [INFO] 서브모듈 정보 동기화 중..." | tee -a $LOG_FILE
git submodule sync --recursive | tee -a $LOG_FILE

# 🔄 서브모듈 업데이트
echo "🔄 [INFO] 서브모듈 업데이트 진행 중..." | tee -a $LOG_FILE
git submodule update --init --recursive --remote 2>&1 | tee -a $LOG_FILE

echo "✅ [SUCCESS] 서브모듈 업데이트 완료!" | tee -a $LOG_FILE