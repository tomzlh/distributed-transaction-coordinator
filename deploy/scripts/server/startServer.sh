#!/bin/bash -xe

java_opt="-Xms1g -Xmx1g"
SERVERJAR="sc-transaction-server.jar"
# 获取工作目录的绝对路径
CWD="`pwd`/$( dirname "${BASH_SOURCE[0]}" )/.."
LIB="./lib"
START_CLASS="com.ops.sc.server.ScServerApplication"
if [[ "$@" == "--version" ]]; then
    cd "${CWD}" && java  -cp ./lib/*: $SERVERJAR --version
    exit 0
fi
echo "start the $SERVERJAR server"
cd "${CWD}" && sh ./bin/stop.sh 2>/dev/null || true
cd "${CWD}" &&  java -Duser.timezone=GMT+08 $java_opt -cp "${SERVERJAR}:${LIB}/*" ${START_CLASS}  2>&1
sleep 2
ps -aux | grep $ENTRANCE | grep -v grep