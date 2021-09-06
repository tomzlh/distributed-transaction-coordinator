#!/bin/bash

scriptPath=`dirname $0`
cd $scriptPath
cd ..

mvn clean assembly:single -P release-sponsor

if [ $# -le 1 ]
then
 project_version=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\['`
 echo $project_version
else
    project_version=$1
fi

rm -rf ./dtc-sponsor-package
mkdir ./dtc-sponsor-package

cd ./target

tar -zxvf dtc-sponsor-assembly.tar.gz -C ../dtc-sponsor-package

cd ../dtc-sponsor-package/dtc

mv sc-transaction-sponsor-$project_version.jar sc-transaction-sponsor.jar