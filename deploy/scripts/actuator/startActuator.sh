#!/bin/bash -xe

java_opt="-Xms1g -Xmx1g"
SERVERJAR="sc-transaction-actuator.jar"
# 获取工作目录的绝对路径
CWD="`pwd`/$( dirname "${BASH_SOURCE[0]}" )/.."
LIB="./lib"
EXTLIB="./extlib"
START_CLASS="com.ops.sc.ta.boot.TaBootApplication"
if [[ "$@" == "--version" ]]; then
    cd "${CWD}" && java  -cp ./lib/*: $SERVERJAR --version
    exit 0
fi
echo "start the $SERVERJAR actuator!"
cd "${CWD}" && sh ./bin/stop.sh 2>/dev/null || true
cd "${CWD}" &&  java -Duser.timezone=GMT+08 $java_opt -cp "${SERVERJAR}:${LIB}/*:${EXTLIB}/*" ${START_CLASS}  2>&1
sleep 2
ps -aux | grep $ENTRANCE | grep -v grep
