#!/bin/bash

APP_NAME="TOOCK-Backend"
DEPLOY_PATH="/home/ubuntu/$APP_NAME"
JAR_NAME=$(ls -t $DEPLOY_PATH/build/libs | grep 'backend.*\.jar$' | grep -v 'plain' | head -n 1)
JAR_PATH="$DEPLOY_PATH/build/libs/$JAR_NAME"
LOG_PATH="$DEPLOY_PATH/deploy.log"

echo "> 배포 시작: $(date)" >> $LOG_PATH

# 1. 기존 애플리케이션 종료
CURRENT_PID=$(pgrep -f $JAR_NAME)
if [ -n "$CURRENT_PID" ]; then
  echo "> 실행 중인 애플리케이션 종료 (PID: $CURRENT_PID)" >> $LOG_PATH
  kill -15 $CURRENT_PID
  sleep 5
fi

# 2. 새 애플리케이션 실행 (dev 프로필 활성화)
echo "> 새 애플리케이션 실행: $JAR_PATH" >> $LOG_PATH
nohup java -jar $JAR_PATH --spring.profiles.active=dev >> $LOG_PATH 2>&1 &
