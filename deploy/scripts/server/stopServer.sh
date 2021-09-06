#!/bin/bash -xe
JAR="sc-transaction-server.jar"
# kill
SPID=`ps -aux | grep ${JAR} | grep -v grep | grep -v kill | awk '{print $2}'`

if [ -n "$SPID" ]; then
  kill ${SPID}
  while kill -0 ${SPID} 2>/dev/null; do echo "sc transaction server is shutting down..."; sleep 1; done
fi
