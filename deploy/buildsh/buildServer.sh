#!/bin/bash

scriptPath=`dirname $0`
cd $scriptPath
cd ..

mvn clean assembly:single -P release-server

if [ $# -le 1 ]
then
 project_version=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\['`
 echo $project_version
else
    project_version=$1
fi

rm -rf ./dtc-server-package
mkdir ./dtc-server-package
#cd ./dtc-server-package

cd ./target

tar -zxvf dtc-server-assembly.tar.gz -C ../dtc-server-package

cd ../dtc-server-package/dtc

mv sc-transaction-server-$project_version.jar sc-transaction-server.jar