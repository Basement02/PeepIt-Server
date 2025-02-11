#!/bin/bash

REPO_PATH="/home/ubuntu/app/src/main/resources"
SUBMODULE_PATH="PeepIt-ServerConfig"
LOG_FILE="/home/ubuntu/app/deploy.log"

echo "🔄 [INFO] 서브모듈 초기화 및 업데이트 시작..." | tee -a $LOG_FILE
cd $REPO_PATH || { echo "❌ [ERROR] 디렉토리 이동 실패: $REPO_PATH" | tee -a $LOG_FILE; exit 1; }

# 🔥 Git이 초기화되지 않았으면 초기화
if [ ! -d ".git" ]; then
    echo "🛠 [INFO] Git 초기화 중..." | tee -a $LOG_FILE
    git init | tee -a $LOG_FILE
    git remote add origin git@github.com:Basement02/PeepIt-ServerConfig.git | tee -a $LOG_FILE
    git fetch --all | tee -a $LOG_FILE
    git reset --hard origin/main | tee -a $LOG_FILE
fi

# SSH Key 설정
eval "$(ssh-agent -s)" | tee -a $LOG_FILE
ssh-add ~/.ssh/id_rsa | tee -a $LOG_FILE

# 서브모듈 업데이트
if [ -d "$REPO_PATH/$SUBMODULE_PATH" ]; then
    echo "🔄 [INFO] 기존 서브모듈 업데이트 중..." | tee -a $LOG_FILE
    git submodule update --recursive --remote 2>&1 | tee -a $LOG_FILE
else
    echo "🆕 [INFO] 서브모듈 초기화 중..." | tee -a $LOG_FILE
    git submodule init 2>&1 | tee -a $LOG_FILE
    git submodule update --init --recursive 2>&1 | tee -a $LOG_FILE
fi

if [ $? -eq 0 ]; then
    echo "✅ [SUCCESS] Git Submodule 업데이트 완료!" | tee -a $LOG_FILE
else
    echo "❌ [ERROR] Git Submodule 업데이트 실패! 로그 확인 필요" | tee -a $LOG_FILE
    exit 1
fi