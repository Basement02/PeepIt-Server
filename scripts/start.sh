#!/usr/bin/env bash

# 프로젝트 설정
PROJECT_ROOT="/home/ubuntu/app"
JAR_SOURCE=$(ls -t $PROJECT_ROOT/build/libs/*.jar 2>/dev/null | head -n 1) # 최신 JAR 찾기
JAR_FILE="$PROJECT_ROOT/spring-webapp.jar"
APP_LOG="$PROJECT_ROOT/application.log"
ERROR_LOG="$PROJECT_ROOT/error.log"
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

# 색상 설정 🎨
RED='\033[0;31m'   # ERROR
GREEN='\033[0;32m' # SUCCESS
YELLOW='\033[0;33m' # WARNING
BLUE='\033[0;34m'   # INFO
NC='\033[0m'       # 기본 색상 (Reset)

# 🔍 JAR 파일 존재 여부 확인 (디버깅)
echo -e "$TIME_NOW ${BLUE}> JAR 파일 존재 여부 확인: $JAR_SOURCE${NC}" | tee -a $DEPLOY_LOG

# JAR 파일 존재 확인 후 복사 🚀
if [ -z "$JAR_SOURCE" ]; then
  echo -e "$TIME_NOW ${RED}> ERROR: JAR 파일이 존재하지 않습니다!${NC}" | tee -a $DEPLOY_LOG
  ls -la "$PROJECT_ROOT/build/libs/"  # 폴더 내부 파일 목록 확인
  exit 1
else
  echo -e "$TIME_NOW ${BLUE}> JAR 파일을 복사합니다: $JAR_SOURCE${NC}" | tee -a $DEPLOY_LOG
  cp "$JAR_SOURCE" "$JAR_FILE"
fi

# Spring Boot 애플리케이션 실행 🚀
echo -e "$TIME_NOW ${YELLOW}> 애플리케이션 실행 중...${NC}" | tee -a $DEPLOY_LOG
nohup java -jar $JAR_FILE > $APP_LOG 2> $ERROR_LOG &

CURRENT_PID=$(pgrep -f $JAR_FILE)
echo -e "$TIME_NOW ${GREEN}> 실행된 프로세스 ID: $CURRENT_PID${NC}" | tee -a $DEPLOY_LOG