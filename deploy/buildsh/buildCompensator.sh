#!/bin/bash

scriptPath=`dirname $0`
cd $scriptPath
cd ..

mvn clean assembly:single -P release-compensator

if [ $# -le 1 ]
then
 project_version=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\['`
 echo $project_version
else
    project_version=$1
fi

rm -rf ./dtc-compensator-package
mkdir ./dtc-compensator-package

cd ./target

tar -zxvf dtc-compensator-assembly.tar.gz -C ../dtc-compensator-package

cd ../dtc-compensator-package/dtc

mv sc-transaction-compensator-$project_version.jar sc-transaction-compensator.jar