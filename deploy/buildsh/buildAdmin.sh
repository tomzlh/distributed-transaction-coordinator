#!/bin/bash

scriptPath=`dirname $0`
cd $scriptPath
cd ..

mvn clean assembly:single -P release-admin

if [ $# -le 1 ]
then
 project_version=`mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\['`
 echo $project_version
else
    project_version=$1
fi

rm -rf ./dtc-admin-package
mkdir ./dtc-admin-package


cd ./target

tar -zxvf dtc-admin-assembly.tar.gz -C ../dtc-admin-package

cd ../dtc-admin-package/dtc

mv sc-transaction-admin-$project_version.jar sc-transaction-admin.jar