#!/bin/bash

REPO_PATH="/home/ubuntu/app"
SUBMODULE_PATH="PeepIt-ServerConfig"  # 서브모듈 폴더 이름

echo "🔄 [INFO] 서브모듈 초기화 및 업데이트 시작..."
cd $REPO_PATH

# 🔥 Git 설정 (서브모듈 인증 문제 해결)
git config --global credential.helper 'cache --timeout=3600'
git config --global --add safe.directory '*'

# 🔥 SSH Key 추가 (EC2에서 GitHub 인증을 위해)
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_rsa  # 🔥 EC2의 SSH Key를 GitHub에 등록해야 함

# 🔥 서브모듈 초기화 및 업데이트
if [ -d "$REPO_PATH/$SUBMODULE_PATH" ]; then
    echo "🔄 [INFO] 기존 서브모듈 업데이트 중..."
    git submodule update --recursive --remote
else
    echo "🆕 [INFO] 서브모듈 초기화 중..."
    git submodule init
    git submodule update --init --recursive
fi

if [ $? -eq 0 ]; then
    echo "✅ [SUCCESS] Git Submodule 업데이트 완료!"
else
    echo "❌ [ERROR] Git Submodule 업데이트 실패!"
    exit 1
fi