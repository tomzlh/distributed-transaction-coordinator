#!/bin/bash

scriptPath=`dirname $0`
cd $scriptPath
cd ..

mvn clean assembly:single -P release-client

if [ $# -le 1 ]
then
 project_version=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\['`
 echo $project_version
else
    project_version=$1
fi

rm -rf ./dtc-client-package
mkdir ./dtc-client-package
#cd ./dtc-client-package

cd ./target

tar -zxvf dtc-client-assembly.tar.gz -C ../dtc-client-package

cd ../dtc-client-package

mkdir extlib

mv sc-transaction-actuator-$project_version.jar sc-transaction-actuator.jar

