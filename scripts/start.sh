#!/usr/bin/env bash

# 프로젝트 설정
PROJECT_ROOT="/home/ubuntu/app"
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

# JAR 파일 존재 확인 후 복사 🚀
if [ -f "$PROJECT_ROOT/build/libs/*.jar" ]; then
  echo -e "$TIME_NOW ${BLUE}> JAR 파일을 복사합니다.${NC}" | tee -a $DEPLOY_LOG
  cp $PROJECT_ROOT/build/libs/*.jar $JAR_FILE
else
  echo -e "$TIME_NOW ${RED}> ERROR: JAR 파일이 존재하지 않습니다!${NC}" | tee -a $DEPLOY_LOG
  exit 1
fi

# Spring Boot 애플리케이션 실행 🚀
echo -e "$TIME_NOW ${YELLOW}> 애플리케이션 실행 중...${NC}" | tee -a $DEPLOY_LOG
nohup java -jar $JAR_FILE > $APP_LOG 2> $ERROR_LOG &

CURRENT_PID=$(pgrep -f $JAR_FILE)
echo -e "$TIME_NOW ${GREEN}> 실행된 프로세스 ID: $CURRENT_PID${NC}" | tee -a $DEPLOY_LOG